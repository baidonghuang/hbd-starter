package com.hbd.starter.redis.serialId.config;

import com.hbd.starter.redis.serialId.enums.PrefixDateFormatEnums;
import com.hbd.starter.redis.serialId.enums.PrefixTypeEnums;
import com.hbd.starter.redis.serialId.enums.ResetCycleEnums;

/**
 * 流水号生成配置
 * created by hbd
 * 2019-06-03
 */
public class SerialIDConfig {

    /**
     * 重置周期（日、月、年）
     */
    private ResetCycleEnums resetCycle;

    /**
     * 日期格式
     */
    private PrefixDateFormatEnums prefixDateFormat;

    /**
     * 前缀格式(1、Prefix_Add_Head_And_Date , 2、Prefix_Add_Head，3、Prefix_AddDate，4、Prefix_Only_Number)
     */
    private PrefixTypeEnums prefixType;

    /**
     * 前缀格式
     */
    private String prefixHead;

    /**
     * 流水号数字位长度
     */
    private Integer numLength = 6;


    public SerialIDConfig() {
        this.prefixHead = "";
        prefixType = PrefixTypeEnums.PREFIX_ADD_HEAD_AND_DATE;
        prefixDateFormat = PrefixDateFormatEnums.PREFIX_FORMAT_YYYYMMDD;
        resetCycle = ResetCycleEnums.RESET_CYCLE_INFINITE; //默认不重置流水号
    }

    public SerialIDConfig(ResetCycleEnums resetCycle, PrefixDateFormatEnums prefixFormat, String prefixHead, PrefixTypeEnums prefixType) {
        this.resetCycle = resetCycle;
        this.prefixDateFormat = prefixFormat;
        this.prefixHead = prefixHead;
        this.prefixType = prefixType;
        if(prefixHead==null) {
            prefixHead = "";
        }
        if(prefixType==null) {
            prefixType = PrefixTypeEnums.PREFIX_ADD_HEAD_AND_DATE;
        }
        if(prefixFormat==null) {
            prefixFormat = PrefixDateFormatEnums.PREFIX_FORMAT_YYYYMMDD;
        }
        if(resetCycle == null) {
            resetCycle = ResetCycleEnums.RESET_CYCLE_DAY;
        }
    }

    public ResetCycleEnums getResetCycle() {
        return resetCycle;
    }

    public void setResetCycle(ResetCycleEnums resetCycle) {
        this.resetCycle = resetCycle;
    }

    public PrefixDateFormatEnums getPrefixDateFormat() {
        return prefixDateFormat;
    }

    public void setPrefixDateFormat(PrefixDateFormatEnums prefixDateFormat) {
        this.prefixDateFormat = prefixDateFormat;
    }

    public PrefixTypeEnums getPrefixType() {
        return prefixType;
    }

    public void setPrefixType(PrefixTypeEnums prefixType) {
        this.prefixType = prefixType;
    }

    public String getPrefixHead() {
        return prefixHead;
    }

    public void setPrefixHead(String prefixHead) {
        this.prefixHead = prefixHead;
    }

    public Integer getNumLength() {
        return numLength;
    }

    public void setNumLength(Integer numLength) {
        this.numLength = numLength;
    }
}
