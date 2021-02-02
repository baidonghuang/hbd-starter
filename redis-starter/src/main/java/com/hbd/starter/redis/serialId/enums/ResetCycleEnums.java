package com.hbd.starter.redis.serialId.enums;

/**
 * 流水号重置周期枚举
 * created by hbd
 * 2019-06-03
 */
public enum ResetCycleEnums {

    /**
     * 流水号重置周期
     */
    RESET_CYCLE_INFINITE("infinite"), //永不重置
    RESET_CYCLE_DAY("day"),//按日重置流水号
    RESET_CYCLE_MONTH("month"),//按月重置流水号
    RESET_CYCLE_YEAR("year");         //按年重置流水号

    // 模块名
    private String name;

    ResetCycleEnums(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
