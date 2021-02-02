package com.hbd.starter.redis.serialId.enums;

/**
 * 流水号前缀日期格式枚举
 * created by hbd
 * 2019-06-03
 */
public enum PrefixDateFormatEnums {
    /**
     * 流水号前缀时间戳格式
     */
    //年月日 20180101
    PREFIX_FORMAT_YYYYMMDD("yyyyMMdd"),
    //短年，月日  180101
    PREFIX_FORMAT_YYMMDD("yyMMdd"),
    //年月 201801
    PREFIX_FORMAT_YYYYMM("yyyyMM");

    // 模块名
    private String name;

    PrefixDateFormatEnums(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
