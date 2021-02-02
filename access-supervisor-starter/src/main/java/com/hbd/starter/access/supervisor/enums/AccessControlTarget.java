package com.hbd.starter.access.supervisor.enums;

/**
 * 访问控制目标枚举
 * @Author: hbd
 * @Date: 2021/01/13
 */
public enum AccessControlTarget {

    //限制IP请求次数
    IP,

    //限制用户请求次数
    USER;

    private AccessControlTarget() {
    }
}
