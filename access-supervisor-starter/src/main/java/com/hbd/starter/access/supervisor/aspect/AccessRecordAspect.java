package com.hbd.starter.access.supervisor.aspect;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FastByteArrayOutputStream;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.hbd.starter.access.supervisor.annotation.AccessRecord;
import com.hbd.starter.access.supervisor.constants.AccessConstants;
import com.hbd.starter.access.supervisor.entity.CtlAccessLogCommonEntity;
import com.hbd.starter.access.supervisor.enums.*;
import com.hbd.starter.access.supervisor.service.LogService;
import com.hbd.starter.access.supervisor.util.BlankUtil;
import com.hbd.starter.redis.constans.RedisDBEnum;
import com.hbd.starter.redis.service.RedisOperator;
import com.hbd.starter.redis.service.RedisOperatorBuilder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * 日志记录切面类
 * created by hbd
 * created date 2021-01-13
 */
@Slf4j
@Aspect
@Component
@ConditionalOnProperty(name = "ctl.access.supervisor.enable", havingValue = "true", matchIfMissing = false)
public class AccessRecordAspect {

    @Resource
    private LogService logService;

    @Pointcut("@annotation(com.hbd.starter.access.supervisor.annotation.AccessRecord)")
    public void pointCut() {}

    /**
     * 在需要同步的方法前后加锁
     * @param jp
     * @return
     * @throws Throwable
     */
    @Around("com.hbd.starter.access.supervisor.aspect.AccessRecordAspect.pointCut()")
    public Object log(ProceedingJoinPoint jp) throws Throwable {
        MethodSignature ms = (MethodSignature) jp.getSignature();
        Method m = ms.getMethod();
        AccessRecord annotation = m.getAnnotation(AccessRecord.class);
        if(annotation != null) {

            /**
             * 1、创建访问记录
             */
            CtlAccessLogCommonEntity logEntity = CtlAccessLogCommonEntity.builder().build();
            int parameterCount = m.getParameterCount();
            RecordStrategy recordType = annotation.recordType();
            if(RecordStrategy.DETAIL == recordType) {
                ParamStrategy paramStrategy = annotation.paramGetType();
                if(ParamStrategy.FROM_METHOD == paramStrategy) {
                    JSONArray params = new JSONArray();
                    if (parameterCount > 0) {
                        for (Object arg : jp.getArgs()) {
                            params.add(arg);
                        }
                        logEntity.setRequestJson(params.toJSONString());
                        if(BlankUtil.isNotEmpty(annotation.requestUniqueKey())) {
                            logEntity.setRequestUniqueId(getRequestUniqueId(params, annotation));
                        }
                    }
                } else {
                    getParamFromHttpRequest(logEntity, annotation);
                }

            }
            //补充其他信息
            fillVisitorInfo(logEntity, annotation);

            /**
             * 2、访问次数限制
             */
            doAccessControl(logEntity, annotation);

            try {
                /**
                 * 3、执行业务逻辑
                 */
                logService.insertLog(logEntity, annotation);
                Object proceed = jp.proceed(jp.getArgs());
                if(annotation.recordType() == RecordStrategy.DETAIL && proceed != null) {
                    logEntity.setResponseJson(JSONObject.toJSONString(proceed, SerializerFeature.WriteDateUseDateFormat));
                }
                return proceed;
            } catch (Exception e) {
                logEntity.setExceptionMessage(e.getMessage());
                logEntity.setStatus(false);
                throw e;
            } finally {
                logService.updateLog(logEntity, annotation);
            }
        } else {
            return jp.proceed(jp.getArgs());
        }
    }

    /**
     * 从方法参数中获取请求唯一标识
     * @param params
     * @param annotation
     * @return
     */
    private String getRequestUniqueId(JSONArray params, AccessRecord annotation) {
        if (params != null && params.size() > 0) {
            for (Object param : params) {
                try {
                    JSONObject paramJson = JSONObject.parseObject(JSONObject.toJSONString(param));
                    if (paramJson.containsKey(annotation.requestUniqueKey())) {
                        return paramJson.getString(annotation.requestUniqueKey());
                    }
                } catch (Exception e) {
                    log.error("从方法参数中获取唯一标识，解析json失败",e);
                }
            }
        }
        return null;
    }

    /**
     * 访问控制
     * @param logEntity
     * @param annotation
     */
    private void doAccessControl(CtlAccessLogCommonEntity logEntity, AccessRecord annotation) {
        RedisOperator redisUtil = RedisOperatorBuilder.getInstance(RedisDBEnum.ACCESS_LOG);
        if(AccessControl.CLOSE != annotation.accessControl()) {
            //默认限制用户，否则限制IP
            String targetKey = logEntity.getVisitorIdentifier();
            if(AccessControlTarget.IP == annotation.controlTarget()) {
                targetKey = logEntity.getRemoteIp();
            }
            //用户访问时间redis_key
            String visitorDateKey = AccessConstants.REDIS_KEY_ACCESS_CONTROL_VISITOR_DATE + annotation.businessType().getId() +":" + targetKey;
            //用户访问次数redis_key
            String visitorTimesKey = AccessConstants.REDIS_KEY_ACCESS_CONTROL_VISITOR_TIMES + annotation.businessType().getId() +":" + targetKey;
            Object o = redisUtil.get(visitorDateKey);
            if(o == null) {
                redisUtil.set(visitorDateKey, DateUtil.now());
                //重置访问次数
                redisUtil.remove(visitorTimesKey);
            } else {
                if(annotation.controlSection() > 0) {
                    Date currentDate = new Date();
                    DateTime oldDate = DateUtil.parse(o.toString());
                    long pastSec = DateUtil.between(oldDate, currentDate, DateUnit.SECOND);
                    if(pastSec > annotation.controlSection()*60) {
                        //重新设置初始访问日期
                        redisUtil.set(visitorDateKey, DateUtil.now());
                        //重置访问次数
                        redisUtil.remove(visitorTimesKey);
                    }
                }
            }
            Long times = redisUtil.incr(visitorTimesKey);
            if(AccessControl.CONTROL_AND_STATISTIC == annotation.accessControl()) {
                //接口访问总次数
                String apiTimesKey = AccessConstants.REDIS_KEY_ACCESS_CONTROL_API_TIMES + annotation.businessType().getId();
                redisUtil.incr(apiTimesKey);
            }
            if(annotation.controlSection() > 0 && annotation.controlTimes() > 0 && times > annotation.controlTimes()) {
                throw new RuntimeException("接口访问频率过快，请稍后再试");
            }
        }
    }

    /**
     * 从http请求中获取传参
     * @return
     * @param logEntity
     * @param annotation
     */
    private String getParamFromHttpRequest(CtlAccessLogCommonEntity logEntity, AccessRecord annotation) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        JSONObject requestJson = new JSONObject();
        if(requestAttributes != null) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            // 获取请求体
            if (request.getContentLength() > 0) {
                try {
                    //先从请求的inputStream中获取，如果没有就从attribute中取streamToBody(streamToBody是CheckSumBuilder中设置的)
                    FastByteArrayOutputStream outputStream = IoUtil.read(request.getInputStream());
                    byte[] body = outputStream.toByteArray();
                    if(body != null) {
                        String requestBody = new String(body, "utf-8");
                        request.setAttribute("streamToBody", requestBody);
                        try {
                            JSONObject bodyJson = JSONObject.parseObject(requestBody);
                            requestJson.put("body", bodyJson);
                            if(BlankUtil.isNotEmpty(annotation.requestUniqueKey())) {
                                if(bodyJson.containsKey(annotation.requestUniqueKey())) {
                                    logEntity.setRequestUniqueId(bodyJson.getString(annotation.requestUniqueKey()));
                                } else {
                                    logEntity.setRequestUniqueId(request.getHeader(annotation.requestUniqueKey()));
                                }
                            }
                        } catch(Exception e) {
                            log.error("解析streamToBody成json失败",e);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        logEntity.setRequestJson(requestJson.toJSONString());
        return requestJson.toJSONString();
    }

    /**
     * 填充其余的日志信息
     * @param logEntity
     * @param annotation
     * @return
     */
    private CtlAccessLogCommonEntity fillVisitorInfo(CtlAccessLogCommonEntity logEntity, AccessRecord annotation) {
        String visitorIdentifier = null;
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if(requestAttributes != null) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            if(SourcePlatform.OPS == annotation.sourcePlatform()) {
                Object userInfo = request.getSession().getAttribute("userInfo");
                if(userInfo != null) {
                    JSONObject userJson = JSONObject.parseObject(JSONObject.toJSONString(userInfo));
                    visitorIdentifier = userJson.getString("account");
                }
            } else if(SourcePlatform.THIRD_PARTY == annotation.sourcePlatform()) {
                visitorIdentifier = request.getHeader("AppKey");
            } else {
                visitorIdentifier = request.getHeader("ctl-user-id");
            }
            logEntity.setSourcePlatform(annotation.sourcePlatform().toString());
            logEntity.setVisitorIdentifier(visitorIdentifier);
            logEntity.setRequestUrl(request.getRequestURL().toString());
            logEntity.setRemoteIp(getClientIp(request));
        }
        Date date = new Date();
        logEntity.setId(DateUtil.format(date,"yyyyMMddHHmmss")+IdUtil.fastSimpleUUID());
        logEntity.setBusinessType(annotation.businessType().getId());
        logEntity.setStatus(true);//默认正常
        logEntity.setCreatedTime(date);
        return logEntity;
    }

    private String getClientIp(HttpServletRequest request) {
        String clientIp = HttpUtil.getClientIP(request,"ctl-client-ip");
        if(BlankUtil.isEmpty(clientIp)) {
            clientIp = request.getRemoteHost();
        }
        return clientIp;
    }
}
