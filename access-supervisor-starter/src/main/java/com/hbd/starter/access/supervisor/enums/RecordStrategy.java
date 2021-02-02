package com.hbd.starter.access.supervisor.enums;

/**
 * 日志记录策略枚举
 * @Author: hbd
 * @Date: 2021/01/13
 */
public enum RecordStrategy {

    //访问记录（仅记录访问路径、时间等信息）
    TRACE,

    //从request中操作记录（记录访问路径、时间、请求参数、请求结果）
    DETAIL;

    private RecordStrategy() {
    }
}
