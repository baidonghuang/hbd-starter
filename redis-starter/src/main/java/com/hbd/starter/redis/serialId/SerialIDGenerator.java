package com.hbd.starter.redis.serialId;

import com.alibaba.fastjson.JSON;
import com.hbd.starter.redis.lock.RedisLockUtil;
import com.hbd.starter.redis.serialId.config.SerialIDConfig;
import com.hbd.starter.redis.serialId.enums.PrefixDateFormatEnums;
import com.hbd.starter.redis.serialId.enums.PrefixTypeEnums;
import com.hbd.starter.redis.serialId.enums.ResetCycleEnums;
import com.hbd.starter.redis.serialId.exception.SerialIDGeneratorException;
import com.hbd.starter.redis.service.RedisUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Redis流水号生成器
 * created by hbd
 * 2019-06-03
 */
public class SerialIDGenerator {

    private static final String LastDateRedisKey = "serial_id_date_map";

    public static String generateSerialID(String key, SerialIDConfig config) {

        //流水号RedisKey
        String serialIdKey = "serial_id_"+key;

        //取出上一次的日期
        Object lastDateObj = RedisUtil.getOneByMapKey(LastDateRedisKey, serialIdKey);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastDate = null;
        if (lastDateObj != null) {
            lastDate = JSON.parseObject(lastDateObj.toString(), LocalDateTime.class);
        } else {
            //若redis无值，则初始化日期
            String lockKey = serialIdKey + "_date_init";
            boolean gotLock = RedisLockUtil.tryLock(5000, 3000, lockKey);
            if(gotLock) {
                lastDateObj = RedisUtil.getOneByMapKey(LastDateRedisKey, serialIdKey);
                if(lastDateObj == null) {
                    RedisUtil.putInMap(LastDateRedisKey, serialIdKey, now);
                    lastDate = now;
                }
            }
        }

        /**
         * 判断是否达到流水号重置条件
         */
        if (ResetCycleEnums.RESET_CYCLE_INFINITE.equals(config.getResetCycle())) {
            //do nothing
        } else {

            /**
             * 如果达到流水号重置条件，则重置流水号为0
             */
            String lockKey = serialIdKey + "_reset_lock_key";
            boolean gotLock = RedisLockUtil.tryLock(5000, 3000, lockKey);
            if(gotLock) {
                lastDateObj = RedisUtil.getOneByMapKey(LastDateRedisKey, serialIdKey);
                lastDate = JSON.parseObject(lastDateObj.toString(), LocalDateTime.class);
                if(checkResetable(now, lastDate, config)) { //重新检查是否需要重置
                    RedisUtil.set(serialIdKey, 0);
                    RedisUtil.putInMap(LastDateRedisKey, serialIdKey, now);
                }
            } else {
                throw new SerialIDGeneratorException("生成流水号失败，无法得到锁");
            }
            RedisLockUtil.unLock(lockKey);
        }

        /**
         * 根据日期格式生成日期前缀
         */
        lastDateObj = RedisUtil.getOneByMapKey(LastDateRedisKey, serialIdKey);
        lastDate = JSON.parseObject(lastDateObj.toString(), LocalDateTime.class);
        String prefixDate = "";
        if(config.getPrefixDateFormat() != null) {
            DateTimeFormatter formatter = null;
            if (PrefixDateFormatEnums.PREFIX_FORMAT_YYYYMMDD == config.getPrefixDateFormat()) {
                formatter = DateTimeFormatter.ofPattern(PrefixDateFormatEnums.PREFIX_FORMAT_YYYYMMDD.getName());
            } else if (PrefixDateFormatEnums.PREFIX_FORMAT_YYYYMM == config.getPrefixDateFormat()) {
                formatter = DateTimeFormatter.ofPattern(PrefixDateFormatEnums.PREFIX_FORMAT_YYYYMM.getName());
            } else if (PrefixDateFormatEnums.PREFIX_FORMAT_YYMMDD == config.getPrefixDateFormat()) {
                formatter = DateTimeFormatter.ofPattern(PrefixDateFormatEnums.PREFIX_FORMAT_YYMMDD.getName());
            } else {
                throw new SerialIDGeneratorException("生成流水号失败，不存在的日期格式："+config.getPrefixDateFormat());
            }
            prefixDate = lastDate.format(formatter);
        }

        /**
         * 根据类型，拼装流水号，
         */
        String serialNo = "";
        long number = RedisUtil.incr(serialIdKey);
        String flowNo = String.format("%0"+config.getNumLength()+"d", number);
        if(PrefixTypeEnums.PREFIX_ADD_HEAD_AND_DATE == config.getPrefixType()) {
            serialNo = config.getPrefixHead() + prefixDate + flowNo;
        } else if(PrefixTypeEnums.PREFIX_ADD_DATE == config.getPrefixType()) {
            serialNo = prefixDate + flowNo;
        } else if(PrefixTypeEnums.PREFIX_ADD_HEAD == config.getPrefixType()) {
            serialNo = config.getPrefixHead() + flowNo;
        } else if(PrefixTypeEnums.PREFIX_ONLY_NUMBER == config.getPrefixType()){
            serialNo = flowNo;
        } else {
            throw new SerialIDGeneratorException("生成流水号失败，不存在的流水号前缀格式："+config.getPrefixType());
        }
        return serialNo;

    }

    private static boolean checkResetable(LocalDateTime now, LocalDateTime lastDate, SerialIDConfig config) {
        if (ResetCycleEnums.RESET_CYCLE_DAY.equals(config.getResetCycle())) {
            if (now.getYear() > lastDate.getYear()
                    || (now.getYear() == lastDate.getYear()
                    && now.getMonth().getValue() > lastDate.getMonth().getValue())
                    || (now.getYear() == lastDate.getYear()
                    && now.getMonth().getValue() == lastDate.getMonth().getValue()
                    && now.getDayOfMonth() > lastDate.getDayOfMonth())) {
                return true;
            }
        } else if (ResetCycleEnums.RESET_CYCLE_MONTH.equals(config.getResetCycle())) {
            if (now.getYear() > lastDate.getYear()
                    || (now.getYear() == lastDate.getYear()
                    && now.getMonth().getValue() > lastDate.getMonth().getValue())) {
                return true;
            }
        } else if (ResetCycleEnums.RESET_CYCLE_YEAR.equals(config.getResetCycle())) {
            if (now.getYear() > lastDate.getYear()) {
                return true;
            }
        } else {
            throw new SerialIDGeneratorException("生成流水号失败，不存在的日期格式重置格式："+config.getResetCycle());
        }
        return false;
    }

}
