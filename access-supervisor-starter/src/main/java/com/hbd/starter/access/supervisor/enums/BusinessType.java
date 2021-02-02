package com.hbd.starter.access.supervisor.enums;

/**
 * 业务类型枚举（用于区分不同的接口记录，以及统计不同接口调用次数）
 * created by hbd
 * created date 2021-01-13
 */
public enum BusinessType {

    //通用
    DEFAULT(0, "通用"),
    USER_FUXING_QUERY_STUDENT(71001, "福星驾校查询学员信息"),
    USER_FUXING_VERIFY_STUDENT(71004, "福星驾校学员认证"),
    MALL_FUXING_UPDATE_OUTSIDE_ORDER(71002, "福星驾校订单回调"),
    COURSEHOUR_GET_USER_LEARN_TIMING(71003, "查询学员学时记录");

    private int id;
    private String name;

    BusinessType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
