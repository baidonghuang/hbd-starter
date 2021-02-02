package com.hbd.starter.access.supervisor.enums;

/**
 * 访问控制枚举
 * @Author: hbd
 * @Date: 2021/01/13
 */
public enum AccessControl {

    //不限制访问
    CLOSE,

    //限制访问次数
    CONTROL_TIMES,

    //访问次数限制，并且统计接口调用总次数
    CONTROL_AND_STATISTIC;

    AccessControl() {
    }
}
