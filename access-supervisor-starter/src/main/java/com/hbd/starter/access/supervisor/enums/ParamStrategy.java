package com.hbd.starter.access.supervisor.enums;

/**
 * 参数获取策略枚举
 * @Author: hbd
 * @Date: 2021/01/13
 */
public enum ParamStrategy {

    //从httpRequest中取请求参数
    FROM_HTTP_REQUEST,
    //从方法中取请求参数
    FROM_METHOD;

    private ParamStrategy() {
    }
}
