package com.hbd.starter.redis.service;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存操作类(替代原先的RedisUtil)
 * created by hbd
 * 2019-01-10
 */
public class RedisUtil {

    private static Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    public static RedisTemplate<Serializable, Object> redisTemplate;


    /**
     * 批量删除对应的value
     *
     * @param keys
     */
    public static void remove(final String... keys) {
        for (String key : keys) {
            remove(key);
        }
    }

    /**
     * 删除对应的value
     *
     * @param key
     */
    public static void remove(final String key) {
        redisTemplate.delete(key);
    }

    /**
     * 判断缓存中是否有对应的value
     *
     * @param key
     * @return
     */
    public static boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }


    /**
     * 读取缓存
     *
     * @param key
     * @return
     */
    public static Object get(final String key) {
        Object result = null;
        ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
        result = operations.get(key);
        return result;
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return
     */
    public static boolean set(final String key, Object value) {
        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.set(key, obj2Json(value));
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 设置过期时间
     *
     * @param key
     * @param time
     * @return
     */
    public static boolean expire(final String key, Long time) {
        boolean result = false;
        try {
            result = redisTemplate.expire(key, time, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 设置过期时间
     *
     * @param key
     * @param timeUnit
     * @return
     */
    public static Long getExpire(final String key, TimeUnit timeUnit) {
        Long result = null;
        try {
            result = redisTemplate.getExpire(key, timeUnit);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @param expireTime  过期时间单位秒
     * @return
     */
    public static boolean set(final String key, Object value, Long expireTime) {
        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.set(key, obj2Json(value));
            redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }


    /**
     * 设置新值，同时返回旧值
     *
     * @param lockKey
     * @param value
     * @return
     */
    public static String getSet(final String lockKey, final String value) {
        String result = redisTemplate.execute((RedisCallback<String>) redisConnection -> {
            byte[] bytes = redisConnection.getSet(lockKey.getBytes(), value.getBytes());
            if (bytes != null) {
                return new String(bytes);
            }
            return null;
        });
        return result;
    }

    /**
     * 如果不存在key则插入
     *
     * @param lockKey
     * @param value
     * @return true 插入成功， false 插入失败
     */
    public static boolean setnx(final String lockKey, final String value) {
        return redisTemplate.execute((RedisCallback<Boolean>) redisConnection -> redisConnection.setNX(lockKey.getBytes(), value.getBytes()));
    }

    /**
     * 如果不存在key则插入
     *
     * @param lockKey
     * @param value
     * @param expireTime  过期时间单位秒
     * @return true 插入成功， false 插入失败
     */
    public static boolean setnx(final String lockKey, final String value, final Long expireTime) {
        Boolean execute = redisTemplate.execute((RedisCallback<Boolean>) redisConnection -> redisConnection.setNX(lockKey.getBytes(), value.getBytes()));
        redisTemplate.expire(lockKey, expireTime, TimeUnit.SECONDS);
        return execute;

    }

    /**
     * 分布式锁
     * @param lockKey
     * @param mapKey
     * @param mapKeyOfLockExpireTime
     * @return
     */
    public static boolean hsetnx(final String lockKey, final String mapKey, final String mapKeyOfLockExpireTime) {
        return redisTemplate.execute((RedisCallback<Boolean>) redisConnection -> redisConnection.hSetNX(lockKey.getBytes(), mapKey.getBytes(), mapKeyOfLockExpireTime.getBytes()));
    }

    /**
     * 判断哈希表里某个键是否存在
     * @param key
     * @param mapKey
     * @return
     */
    public static boolean hexists(final String key, final String mapKey) {
        return redisTemplate.execute((RedisCallback<Boolean>) redisConnection -> redisConnection.hExists(key.getBytes(), mapKey.getBytes()));
    }


    /**
     * 删除键值
     * @param key
     * @param mapKey
     */
    public static void hremove(final String key, List<String> mapKey ) {
        for (String item : mapKey) {
            redisTemplate.execute((RedisCallback<Long>) redisConnection -> redisConnection.hDel(key.getBytes(), item.getBytes()));
        }
    }

    /**
     * 叠加信息
     * @param mapKey
     * @param mapValue
     * @param num
     * @return
     */
    public static long hincrby(final String mapKey, final String mapValue, final long num) {
        return redisTemplate.execute((RedisCallback<Long>) redisConnection -> redisConnection.hIncrBy(mapKey.getBytes(), mapValue.getBytes(), num));
    }

    /**
     * 设置过期时间（单位秒）
     *
     * @param lockKey
     * @param stringOfLockExpireTime
     * @return true 插入成功， false 插入失败
     */
    public static boolean setEx(final String lockKey, final String value, final long stringOfLockExpireTime) {
        return redisTemplate.execute((RedisCallback<Boolean>) redisConnection -> {
            try {
                redisConnection.setEx(lockKey.getBytes(), stringOfLockExpireTime, value.getBytes());
            } catch (Exception e) {
                return false;
            }
            return true;
        });
    }
    /**
     * 设置过期时间（单位毫秒）
     * @param lockKey
     * @param stringOfLockExpireTime
     * @return true 插入成功， false 插入失败
     */
    public static boolean pSetEx(final String lockKey, final String value, final long stringOfLockExpireTime) {
        return redisTemplate.execute((RedisCallback<Boolean>) redisConnection -> {
            try {
                redisConnection.pSetEx(lockKey.getBytes(), stringOfLockExpireTime, value.getBytes());
            } catch (Exception e) {
                return false;
            }
            return true;
        });
    }

    /**
     * 自增
     *
     * @param key
     * @return
     */
    public static Long incr(final String key) {
        return redisTemplate.execute((RedisCallback<Long>) redisConnection -> redisConnection.incr(key.getBytes()));
    }

    /**
     * 指定步长自增
     * @param key
     * @return
     */
    public static Long incrBy(final String key, final long size) {
        return redisTemplate.execute((RedisCallback<Long>) redisConnection -> redisConnection.incrBy(key.getBytes(), size));
    }

    /**
     * 自减
     *
     * @param key
     * @return
     */
    public static Long decr(final String key) {
        return redisTemplate.execute((RedisCallback<Long>) redisConnection -> redisConnection.decr(key.getBytes()));
    }


    /**
     * setnx 和 getSet方式插入的数据，调用此方法获取
     *
     * @param key
     * @return
     */
    public static String getInExecute(final String key) {
        String result = redisTemplate.execute((RedisCallback<String>) redisConnection -> {
            byte[] bytes = redisConnection.get(key.getBytes());
            if (bytes == null) {
                return null;
            } else {
                return new String(bytes);
            }
        });
        return result;
    }


    /**
     * 将缓存保存在map集合中
     *
     * @param redisKey
     * @param mapKey
     * @param mapValue
     * @return
     */
    public static boolean putInMap(final String redisKey, String mapKey, Object mapValue) {
        boolean result = false;
        try {
            HashOperations<Serializable, Object, Object> operations = redisTemplate.opsForHash();
            operations.put(redisKey, mapKey, obj2Json(mapValue));
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 将缓存保存在map集合中
     *
     * @param redisKey
     * @param map
     * @return
     */
    public static boolean putInMap(final String redisKey, Map map) {
        boolean result = false;
        try {
            HashOperations<Serializable, Object, Object> operations = redisTemplate.opsForHash();
            if(map != null) {
                for (Object key : map.keySet()) {
                    map.put(key, obj2Json(map.get(key)));
                }
            }
            operations.putAll(redisKey, map);
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 通过key从Map集合中获取value
     *
     * @param redisKey
     * @param mapKey
     * @return
     */
    public static Object getOneByMapKey(final String redisKey, String mapKey) {
        HashOperations<Serializable, Object, Object> operations = redisTemplate.opsForHash();
        return operations.get(redisKey, mapKey);
    }

    /**
     * 通过多个key从Map集合获取多个value
     *
     * @param redisKey
     * @return
     */
    public static Object getMultiByMapKeys(final String redisKey, Collection mapKeys) {
        HashOperations<Serializable, Object, Object> operations = redisTemplate.opsForHash();
        return operations.multiGet(redisKey, mapKeys);
    }

    /**
     * 获取Map集合的所有value
     *
     * @param redisKey
     * @return
     */
    public static Object getAllFromMap(final String redisKey) {
        HashOperations<Serializable, Object, Object> operations = redisTemplate.opsForHash();
        return operations.values(redisKey);
    }

    /**
     * 获取Map集合的所有值
     *
     * @param redisKey
     * @return
     */
    public static Map getEntriesFromMap(final String redisKey) {
        HashOperations<Serializable, Object, Object> operations = redisTemplate.opsForHash();
        return operations.entries(redisKey);
    }


    /**
     * 删除Map中的一个键值对
     *
     * @param redisKey
     * @param mapKey
     */
    public static void removeOneFromMap(final String redisKey, Object mapKey) {
        HashOperations<Serializable, Object, Object> operations = redisTemplate.opsForHash();
        operations.delete(redisKey, mapKey);
    }

    /**
     * 删除Map中的多个键值对
     *
     * @param redisKey
     * @param mapKeys
     */
    public static void removeMultiFromMap(final String redisKey, Object... mapKeys) {
        HashOperations<Serializable, Object, Object> operations = redisTemplate.opsForHash();
        operations.delete(redisKey, mapKeys);
    }

    /**
     * 添加一条记录到List集合
     *
     * @param redisKey
     * @param value
     * @return
     */
    public static boolean addInList(final String redisKey, Object value) {
        boolean result = false;
        try {
            ListOperations<Serializable, Object> listOperations = redisTemplate.opsForList();
            listOperations.leftPush(redisKey, obj2Json(value));
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 添加一条记录到List集合
     *
     * @param redisKey
     * @param value
     * @return
     */
    public static boolean addListInList(final String redisKey, List value) {
        boolean result = false;
        try {
            ListOperations<Serializable, Object> listOperations = redisTemplate.opsForList();
            listOperations.leftPushAll(redisKey, value);

            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 删除list中一条记录
     * @param redisKey
     * @param value
     * @return
     */
    public static boolean removeOneFromList(final String redisKey, Object value) {
        boolean result = false;
        try {
            ListOperations<Serializable, Object> listOperations = redisTemplate.opsForList();
            listOperations.remove(redisKey, 0 , value);
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 根据起始start截止end区间获取list中多条记录
     * @param redisKey
     * @param start
     * @param end
     * @return
     */
    public static List<Object> rangeList(final String redisKey, long start, long end) {
        try {
            ListOperations<Serializable, Object> listOperations = redisTemplate.opsForList();
            return listOperations.range(redisKey, start, end);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 根据起始start截止end区间删除list中多条记录
     * @param redisKey
     * @param start
     * @param end
     * @return
     */
    public static boolean trimList(final String redisKey, long start, long end) {
        boolean result = false;
        try {
            ListOperations<Serializable, Object> listOperations = redisTemplate.opsForList();
            listOperations.trim(redisKey, start, end);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 得到List集合所有记录
     *
     * @param redisKey
     * @return
     */
    public static Object getList(final String redisKey) {
        ListOperations<Serializable, Object> listOperations = redisTemplate.opsForList();
        return listOperations.range(redisKey, 0, listOperations.size(redisKey));
    }

    /**
     * 注意，每个微服务初始化时固定绑定一个DB，不要切换DB。
     * 因为redisTemplate是静态的切换DB可能会错误先后覆盖
     * @param dbIndex
     */
    public static void changeDB(int dbIndex) {
        LettuceConnectionFactory jedisConnectionFactory = (LettuceConnectionFactory) redisTemplate.getConnectionFactory();
        jedisConnectionFactory.setDatabase(dbIndex);
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        jedisConnectionFactory.resetConnection();
    }

    /**
     * 读取缓存-根据前缀批量获取
     * @param prefix
     * @return
     */
    public static Set<Serializable> getByPrefix(final String prefix) {
        return redisTemplate.keys(prefix + "*");
    }

    /** 
    * @Description: 消息发布
    * @Param: [channel, message] 
    * @return: void 
    * @Author: yjr
    * @Date: 2020/3/17 
    */ 
    public static void convertAndSend(String channel, Object message){
        redisTemplate.convertAndSend(channel,message);
    }

    /**
     * 讲对象转成JSON
     */
    public static Object obj2Json(Object value) {
        if(value == null || value instanceof String) {
            return value;
        }
        return JSON.toJSONString(value);
    }

}
