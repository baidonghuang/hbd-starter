package com.hbd.starter.redis.util;

import com.hbd.starter.redis.lock.RedisLockUtil;
import com.hbd.starter.redis.service.RedisUtil;

import java.util.*;

public class RandomService {

    public static final String RandomPoolKeyPrefix = "random_pool_";
    public static final String RandomLockKeyPrefix = "random_lock_";
    public static final String RandomSerialNumKeyPrefix = "random_serial_number_";
    public static final int PoolSize = 1000;
    public static final int RandomLength = 8;


    /**
     * 随机数池，一次批量生成指定数量的随机数
     * @param randomKey   随机数redisKey
     * @param batchSize   批量生成大小
     * @return
     */
    public static List<String> generateRandomPool(String randomKey, int batchSize) {
        String poolKey = RandomPoolKeyPrefix + randomKey;
        String serialNumberKey = RandomSerialNumKeyPrefix + randomKey;
        List<String> poolList = new ArrayList<>(batchSize);
        //获取有序数字数值
        Object initialNumber = RedisUtil.get(serialNumberKey);
        if(initialNumber == null) {
            //初始化随机数的有序数字数值
            initialNumber = "0";
            RedisUtil.set(serialNumberKey, 0);
        }

        //将有序数字转换为随机数
        long serialNumber = Long.parseLong(initialNumber.toString());
        for (long i = 0; i < batchSize; i++) {
            String randomStr = RandomUtil.generateRandomStr(serialNumber, RandomLength);
            poolList.add(randomStr);
            serialNumber++;
        }

        // 按步长递增有序数字数值
        RedisUtil.incrBy(serialNumberKey, batchSize);

        // 将随机数池写入redis
        RedisUtil.addListInList(poolKey, poolList);

        return (List<String>) RedisUtil.getList(poolKey);
    }

    /**
     * 从随机数池中获取随机数
     * @param randomKey
     * @return
     */
    public static String getOneFromRandomPool(String randomKey) {
        while(true) {
            //加锁防止并发获取相同code
            String lockKey = RandomLockKeyPrefix + randomKey;
            if (RedisLockUtil.tryLock(3000, 5000, lockKey)) {
                String randomStr = null;
                String poolKey = RandomPoolKeyPrefix + randomKey;
                List<String> poolList = (List<String>) RedisUtil.getList(poolKey);
                if (poolList == null || poolList.size() == 0) {
                    //如果随机数池没有数据，更新随机数池
                    poolList = generateRandomPool(randomKey, PoolSize);
                }
                if (poolList == null || poolList.size() == 0) {
                    throw new RuntimeException("随机数池创建失败");
                }
                Random r = new Random();
                randomStr = poolList.get(r.nextInt(poolList.size()));//随机返回池中一个随机数
                RedisUtil.removeOneFromList(poolKey, randomStr);    //随机数用完从池中删除
                RedisLockUtil.unLock(lockKey);
                return randomStr;
            }
        }
    }

    /**
     * 批量从随机数池中获取随机数
     * @param randomKey
     * @return
     */
    public static Set<String> getBatchFromRandomPool(String randomKey, int batchSize) {
        while(true) {
            //加锁防止并发获取相同code
            String lockKey = RandomLockKeyPrefix + randomKey;
            if (RedisLockUtil.tryLock(3000, 5000, lockKey)) {
                String randomStr = null;
                String poolKey = RandomPoolKeyPrefix + randomKey;
                List<String> poolList = (List<String>) RedisUtil.getList(poolKey);
                if (poolList == null || poolList.size() < batchSize) {
                    //如果随机数池没有数据，更新随机数池
                    poolList = generateRandomPool(randomKey, batchSize > PoolSize ? batchSize : PoolSize);
                }
                if (poolList == null || poolList.size() == 0) {
                    throw new RuntimeException("随机数池创建失败");
                }
                List<String> codes = (List)RedisUtil.rangeList(poolKey, 0, batchSize - 1);
                RedisUtil.trimList(poolKey, batchSize,  poolList.size());
                RedisLockUtil.unLock(lockKey);
                return new HashSet<>(codes);
            }
        }
    }

}
