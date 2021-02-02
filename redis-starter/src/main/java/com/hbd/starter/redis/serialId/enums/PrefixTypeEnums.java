package com.hbd.starter.redis.serialId.enums;

/**
 * 流水号前缀格式枚举
 * created by hbd
 * 2019-06-03
 */
public enum PrefixTypeEnums {

    // 流水号前缀增加自定义头+日期格式+数字序列，例：SZ2018010100001
    PREFIX_ADD_HEAD_AND_DATE("1"),
    // 流水号显示自定义头+数字序列，例：SZ0000001
    PREFIX_ADD_HEAD("2"),
    // 流水号显示日期+数字序列，例：2018010100001
    PREFIX_ADD_DATE("3"),
    // 流水号只显示数字序列，例：00001
    PREFIX_ONLY_NUMBER("4");

    // 模块名
    private String name;

    PrefixTypeEnums(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
