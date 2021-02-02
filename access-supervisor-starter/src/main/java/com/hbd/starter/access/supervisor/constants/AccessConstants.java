package com.hbd.starter.access.supervisor.constants;

/**
 * 访问监察公共常量类
 * created by hbd
 * 2021-01-26
 */
public class AccessConstants {

    //api访问总次数
    public static final String REDIS_KEY_ACCESS_CONTROL_API_TIMES = "access_control_api_times:";

    //用户访问总次数
    public static final String REDIS_KEY_ACCESS_CONTROL_VISITOR_TIMES = "access_control_visitor_times:";

    //用户访问时间
    public static final String REDIS_KEY_ACCESS_CONTROL_VISITOR_DATE = "access_control_visitor_date:";

}
