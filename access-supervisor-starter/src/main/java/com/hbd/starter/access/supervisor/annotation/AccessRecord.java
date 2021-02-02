package com.hbd.starter.access.supervisor.annotation;

import com.hbd.starter.access.supervisor.enums.*;
import com.hbd.starter.access.supervisor.enums.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 日志记录注解：
 * 1、用于记录接口请求信息
 * 2、限制接口频率
 * created by hbd
 * created date 2021-01-13
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface AccessRecord {

    /**
     * OPS：用户唯一标识从session中获取userInfo.account
     * THIRD_PARTY：用户唯一标识从header中取AppKey
     * 其余类型：用户唯一标识从header中取ctl-user-id
     * @return
     */
    SourcePlatform sourcePlatform() default SourcePlatform.DEFAULT;

    /**
     * TRACE、记录操作方法，操作时间，操作人
     * DETAIL、记录操作方法，参数，操作时间，操作人
     * @return
     */
    RecordStrategy recordType() default RecordStrategy.DETAIL;

    /**
     * httpRequest中取请求参数
     *     FROM_HTTP_REQUEST
     * 从方法中取请求参数
     *     FROM_METHOD
     * @return
     */
    ParamStrategy paramGetType() default ParamStrategy.FROM_METHOD;

    /**
     * 持久化策略（DB、FILE、LOG）
     * @return
     */
    PersistenceStrategy persistence() default PersistenceStrategy.ASYNC_DB;

    /**
     * 日志记录表名后缀ctl_access.ctl_access_log_common(后缀可配置)
     * @return
     */
    String tableSuffix() default "common";

    /**
     * 业务类型
     * @return
     */
    BusinessType businessType() default BusinessType.DEFAULT;

    /**
     * 请求唯一标识
     * @return
     */
    String requestUniqueKey() default "";

    /**
     * 伪意见
     */

    /**
     * 访问控制方式枚举
     *  不限制访问
     *     CLOSE
     *  限制访问次数
     *     CONTROL_TIMES
     *   访问次数限制，并且统计接口调用总次数
     *     CONTROL_AND_STATISTIC
     * @return
     */
    AccessControl accessControl() default AccessControl.CLOSE;

    /**
     * 次数限制目标（USER， IP）
     * @return
     */
    AccessControlTarget controlTarget() default AccessControlTarget.USER;

    /**
     * 次数限制（配置大于0才生效）
     */
    int controlTimes() default -1;

    /**
     * 访问限制时间段(单位：分钟）
     */
    int controlSection() default 1;

}
