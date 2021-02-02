package com.hbd.starter.redis.lock.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hbd.starter.redis.lock.RedisLockUtil;
import com.hbd.starter.redis.lock.annotation.RedisLock;
import com.hbd.starter.redis.lock.enums.RedisLockKeyStrategy;
import com.hbd.starter.redis.lock.exception.RedisLockException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;


/**
 * Redis分布式锁切面类
 * created by hbd
 * created date 2019-06-03
 */
@Aspect
@Component
public class RedisLockAspect {

	private static org.slf4j.Logger logger = LoggerFactory.getLogger(RedisLockAspect.class);

	@Pointcut("@annotation(com.hbd.starter.redis.lock.annotation.RedisLock)")
	public void pointCut() {}

	/**
	 * 在需要同步的方法前后加锁
	 * @param jp
	 * @return
	 * @throws Throwable
	 */
	@Around("com.hbd.starter.redis.lock.aspect.RedisLockAspect.pointCut()")
	public Object payingOrderSyncLock(ProceedingJoinPoint jp) throws Throwable {
		Object o = null;
		MethodSignature ms = (MethodSignature) jp.getSignature();
		Method m = ms.getMethod();
		RedisLock annotation = m.getAnnotation(RedisLock.class);
		if(annotation == null) {
			o = jp.proceed(jp.getArgs());
		} else {

			//如果自定义lockKey为空，默认拦截的类名+方法名作为lockKey
			String lockKey = annotation.lockKey();
			if(lockKey == null || "".equals(lockKey)) {
				lockKey = m.getDeclaringClass().getName()+"."+m.getName();
			}

			//如果lockKey不是CUSTOM模式，取方法的参数值作为lockkey的一部分。
			if(RedisLockKeyStrategy.CUSTOM != annotation.lockStrategy()) {
				int parameterCount = m.getParameterCount();
				if(parameterCount > 0) {
					int paramPosition = annotation.paramPosition();
					if(parameterCount < paramPosition +1) {
						throw new RedisLockException("400", "paramPosition的数值"+paramPosition+",大于该方法的参数个数"+parameterCount+"："+lockKey);
					}
					Object parameter = jp.getArgs()[paramPosition];
					String parameterValue = null;
					if(parameter != null) {
						if(RedisLockKeyStrategy.PARAM_BASE == annotation.lockStrategy()) {
							parameterValue = String.valueOf(parameter);
						} else if(RedisLockKeyStrategy.PARAM_OBJECT == annotation.lockStrategy()) {
							JSONObject jsonParam = JSON.parseObject(JSON.toJSONString(parameter));
							if(annotation.paramName() != null && !"".equals(annotation.paramName())) {
								parameterValue = getValue(jsonParam, annotation.paramName());
							} else {
								throw new RedisLockException("400", "LockKeyStrategy为ParamObject，但是ParamName名称为空");
							}
						}
						if(parameterValue != null && !"".equals(parameterValue)) {
							lockKey = lockKey+"_"+parameterValue;
						} else {
							logger.warn("==============>  无法生成LockKey，["+paramPosition+"]参数值为空,将启用默认值："+lockKey);
						}
					} else {
						logger.warn("==============>  无法生成LockKey，["+paramPosition+"]参数值为空,将启用默认值："+lockKey);
					}
				}
			}
			if(RedisLockUtil.tryLock(annotation.waitTime(), annotation.leaseTime(), lockKey)) {
				logger.warn("===============>获取分布式锁,lockKey:"+lockKey);
				try {
					o = jp.proceed(jp.getArgs());
				} finally {
					RedisLockUtil.unLock(lockKey);
				}
			} else {
				throw new RedisLockException("408", "获取锁超时,max wait time "+annotation.waitTime());
			}
		}

		return o;
	}

	/**
	 * 获取复杂对象中的属性值
	 * @param jsonParam
	 * @param paramName
	 * @return
	 */
	private String getValue(JSONObject jsonParam, String paramName) {
		String value = null;
		if(paramName.contains(".")) {
			String[] properties = paramName.split("\\.");
			JSONObject tempObj = jsonParam;
			for (int i=0; i<properties.length; i++) {
				if(i<properties.length-1) {
					tempObj = jsonParam.getJSONObject(properties[i]);
					if(tempObj==null) {
						throw new RedisLockException("400", "RedisLock.ParamName参数名不存在："+paramName);
					}
				} else {
					value = tempObj.getString(properties[i]);
				}
			}
		} else {
			value = jsonParam.getString(paramName);
		}
		return value;
	}
}
