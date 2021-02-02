package com.hbd.starter.access.supervisor.enums;

/**
 * 持久化策略枚举
 * @Author: hbd
 * @Date: 2021/01/13
 */
public enum PersistenceStrategy {

    //日志打印
    LOG,

    //记录到文件
    FILE,

    //同步记录到数据库
    SYNC_DB,

    //异步记录到数据库
    ASYNC_DB;

    private PersistenceStrategy() {
    }
}
