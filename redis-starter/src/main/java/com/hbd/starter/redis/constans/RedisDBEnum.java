package com.hbd.starter.redis.constans;

public enum RedisDBEnum {

    DEFAULT(0, "默认库", "", "json"),
    ACCESS_LOG(1, "日志库", "access_supervisor_","json");

    // redis库
    private Integer index;
    // 模块名
    private String name;
    // key前缀
    private String keyPrefix;
    // serializer
    private String serializer;

    RedisDBEnum(Integer index, String name, String keyPrefix, String serializer) {
        this.index = index;
        this.name = name;
        this.keyPrefix = keyPrefix;
        this.serializer = serializer;
    }

    public Integer getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public String getSerializer() {
        return serializer;
    }

}
