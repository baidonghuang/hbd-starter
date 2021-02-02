package com.hbd.starter.access.supervisor.enums;

/**
 * 来源平台枚举（通过此枚举区分用户唯一标识的获取方式）
 * created by hbd
 * created date 2021-01-13
 */
public enum SourcePlatform {

    //通用（用户唯一标识从header中取ctl-user-id）
    DEFAULT,

    //运维平台(用户唯一标识从header中取AppKey)
    OPS,

    //运营平台（用户唯一标识从header中取ctl-user-id）
    MANAGER,

    //服务平台（用户唯一标识从header中取ctl-user-id）
    FW,

    //app（用户唯一标识从header中取ctl-user-id）
    APP,

    //第三方（用户唯一标识从header中取AppKey）
    THIRD_PARTY;

    SourcePlatform() {
    }
}
