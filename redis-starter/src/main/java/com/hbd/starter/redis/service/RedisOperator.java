package com.hbd.starter.redis.service;

import com.hbd.starter.redis.constans.RedisDBEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.*;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 本类允许操作不同的redis数据库,
 * 设置的key统一加上各个模块枚举的key前缀
 * 模块与库对应关系参看RedisDBEnum类
 * 本类实例通过RedisOperatorBuilder获取，不允许自己new
 * created by hbd
 * 2019-02-21
 */
public class RedisOperator {

    private Logger logger = LoggerFactory.getLogger(RedisOperator.class);

    private RedisTemplate<Serializable, Object> redisTemplate;

    private RedisDBEnum redisDBEnum;

    public void setRedisTemplate(RedisTemplate<Serializable, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setRedisDBEnum(RedisDBEnum redisDBEnum) {
        this.redisDBEnum = redisDBEnum;
    }

    private String buildKey(String key) {
        return redisDBEnum.getKeyPrefix() + key;
    }

    /**
     * 批量删除对应的value
     *
     * @param keys
     */
    public void remove(final String... keys) {
        for (String key : keys) {
            remove(buildKey(key));
        }
    }

    /**
     * 删除对应的value
     *
     * @param redisKey
     */
    public void remove(final String redisKey) {
        if (exists(redisKey)) {
            redisTemplate.delete(buildKey(redisKey));
        }
    }

    /**
     * 通过前缀删除对应的value
     * @param prefix
     */
    public void removeByPrefix(String prefix) {
        Set<Serializable> keys = redisTemplate.keys(buildKey(prefix) + "*");
        if (keys!=null){
            redisTemplate.delete(keys);
        }
    }

    /**
     * 判断缓存中是否有对应的value
     *
     * @param redisKey
     * @return
     */
    public boolean exists(final String redisKey) {
        return redisTemplate.hasKey(buildKey(redisKey));
    }


    /**
     * 读取缓存
     *
     * @param redisKey
     * @return
     */
    public Object get(final String redisKey) {
        Object result = null;
        ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
        result = operations.get(buildKey(redisKey));
        return result;
    }

    /**
     * 写入缓存
     *
     * @param redisKey
     * @param value
     * @return
     */
    public boolean set(final String redisKey, Object value) {
        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            if("json".equals(redisDBEnum.getSerializer())) {
                value = RedisUtil.obj2Json(value);
            }
            operations.set(buildKey(redisKey), value);
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 设置过期时间
     *
     * @param redisKey
     * @param time
     * @return
     */
    public boolean expire(final String redisKey, Long time) {
        boolean result = false;
        try {
            result = redisTemplate.expire(buildKey(redisKey), time, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 设置过期时间
     *
     * @param redisKey
     * @param timeUnit
     * @return
     */
    public Long getExpire(final String redisKey, TimeUnit timeUnit) {
        Long result = null;
        try {
            result = redisTemplate.getExpire(buildKey(redisKey), timeUnit);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 写入缓存
     *
     * @param redisKey
     * @param value
     * @return
     */
    public boolean set(final String redisKey, Object value, Long expireTime) {
        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            if("json".equals(redisDBEnum.getSerializer())) {
                value = RedisUtil.obj2Json(value);
            }
            operations.set(buildKey(redisKey), value);
            redisTemplate.expire(buildKey(redisKey), expireTime, TimeUnit.SECONDS);
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }


    /**
     * 设置新值，同时返回旧值
     *
     * @param redisKey
     * @param value
     * @return
     */
    public String getSet(final String redisKey, final String value) {
        String result = redisTemplate.execute((RedisCallback<String>) redisConnection -> {
            byte[] bytes = redisConnection.getSet(buildKey(redisKey).getBytes(), value.getBytes());
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
     * @param redisKey
     * @param stringOfLockExpireTime
     * @return true 插入成功， false 插入失败
     */
    public boolean setnx(final String redisKey, final String stringOfLockExpireTime) {
        return redisTemplate.execute((RedisCallback<Boolean>) redisConnection -> redisConnection.setNX(buildKey(redisKey).getBytes(), stringOfLockExpireTime.getBytes()));
    }

    /**
     * 自增1，自减1
     *
     * @param redisKey
     * @return
     */
    public Long incr(final String redisKey) {
        return redisTemplate.execute((RedisCallback<Long>) redisConnection -> redisConnection.incr(buildKey(redisKey).getBytes()));
    }

    /**
     * 自增，自减 指定数字
     *
     * @param redisKey
     * @param num 加减值  加正数  减负数
     * @return
     */
    public Long incrBy(final String redisKey, Long num) {
        return redisTemplate.execute((RedisCallback<Long>) redisConnection -> redisConnection.incrBy(buildKey(redisKey).getBytes(), num));
    }


    /**
     * setnx 和 getSet方式插入的数据，调用此方法获取
     *
     * @param redisKey
     * @return
     */
    public String getInExecute(final String redisKey) {
        String result = redisTemplate.execute((RedisCallback<String>) redisConnection -> {
            byte[] bytes = redisConnection.get(buildKey(redisKey).getBytes());
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
    public boolean putInMap(final String redisKey, String mapKey, Object mapValue) {
        boolean result = false;
        try {
            HashOperations<Serializable, Object, Object> operations = redisTemplate.opsForHash();
            if("json".equals(redisDBEnum.getSerializer())) {
                mapValue = RedisUtil.obj2Json(mapValue);
            }
            operations.put(buildKey(redisKey), mapKey, mapValue);
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
    public boolean putInMap(final String redisKey, Map map) {
        boolean result = false;
        try {
            HashOperations<Serializable, Object, Object> operations = redisTemplate.opsForHash();
            if("json".equals(redisDBEnum.getSerializer()) && map!=null) {
                for (Object key : map.keySet()) {
                    map.put(key, RedisUtil.obj2Json(map.get(key)));
                }
            }
            operations.putAll(buildKey(redisKey), map);
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 将缓存保存在map集合中，并设置过期时间（秒）
     *
     * @author yangqh
     * @date 2019/3/11
     **/
    public boolean putInMap(final String redisKey, Map map, Long expire) {
        boolean result = false;
        try {
            HashOperations<Serializable, Object, Object> operations = redisTemplate.opsForHash();
            if("json".equals(redisDBEnum.getSerializer()) && map!=null) {
                for (Object key : map.keySet()) {
                    map.put(key, RedisUtil.obj2Json(map.get(key)));
                }
            }
            operations.putAll(buildKey(redisKey), map);
            redisTemplate.expire(buildKey(redisKey), expire, TimeUnit.SECONDS);
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
    public Object getOneByMapKey(final String redisKey, String mapKey) {
        HashOperations<Serializable, Object, Object> operations = redisTemplate.opsForHash();
        Object result = operations.get(buildKey(redisKey), mapKey);
        return result;
    }

    /**
     * 通过多个key从Map集合获取多个value
     *
     * @param redisKey
     * @return
     */
    public Object getMultiByMapKeys(final String redisKey, Collection mapKeys) {
        HashOperations<Serializable, Object, Object> operations = redisTemplate.opsForHash();
        List list = operations.multiGet(buildKey(redisKey), mapKeys);
        return list;
    }

    /**
     * 获取Map集合的所有value
     *
     * @param redisKey
     * @return
     */
    public Object getAllFromMap(final String redisKey) {
        HashOperations<Serializable, Object, Object> operations = redisTemplate.opsForHash();
        List list = operations.values(buildKey(redisKey));
        return list;
    }

    /**
     * 获取Map集合的所有值
     *
     * @param redisKey
     * @return
     */
    public Map getEntriesFromMap(final String redisKey) {
        HashOperations<Serializable, Object, Object> operations = redisTemplate.opsForHash();
        Map<Object, Object> entries = operations.entries(buildKey(redisKey));
        return entries;
    }


    /**
     * 删除Map中的一个键值对
     *
     * @param redisKey
     * @param mapKey
     */
    public void removeOneFromMap(final String redisKey, Object mapKey) {
        HashOperations<Serializable, Object, Object> operations = redisTemplate.opsForHash();
        operations.delete(buildKey(redisKey), mapKey);
    }

    /**
     * 删除Map中的多个键值对
     *
     * @param redisKey
     * @param mapKeys
     */
    public void removeMultiFromMap(final String redisKey, Object... mapKeys) {
        HashOperations<Serializable, Object, Object> operations = redisTemplate.opsForHash();
        operations.delete(buildKey(redisKey), mapKeys);
    }

    /**
     * 添加一条记录到List集合
     *
     * @param redisKey
     * @param value
     * @return
     */
    public boolean addInList(final String redisKey, Object value) {
        boolean result = false;
        try {
            ListOperations<Serializable, Object> listOperations = redisTemplate.opsForList();
            if("json".equals(redisDBEnum.getSerializer()) && value != null) {
                listOperations.leftPush(buildKey(redisKey), RedisUtil.obj2Json(value));
            } else {
                listOperations.leftPush(buildKey(redisKey), value);
            }
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    public boolean addInList(final String redisKey, Object value, Long expire) {
        boolean result = false;
        try {
            ListOperations<Serializable, Object> listOperations = redisTemplate.opsForList();
            if("json".equals(redisDBEnum.getSerializer()) && value != null) {
                listOperations.leftPush(buildKey(redisKey), RedisUtil.obj2Json(value));
            } else {
                listOperations.leftPush(buildKey(redisKey), value);
            }
            redisTemplate.expire(buildKey(redisKey), expire, TimeUnit.SECONDS);
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    public boolean addInListOfRight(final String redisKey, Object value) {
        boolean result = false;
        try {
            ListOperations<Serializable, Object> listOperations = redisTemplate.opsForList();
            if("json".equals(redisDBEnum.getSerializer()) && value != null) {
                listOperations.rightPush(buildKey(redisKey), RedisUtil.obj2Json(value));
            } else {
                listOperations.rightPush(buildKey(redisKey), value);
            }
            result = true;
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
    public Object getList(final String redisKey) {
        ListOperations<Serializable, Object> listOperations = redisTemplate.opsForList();
        List<Object> list = listOperations.range(buildKey(redisKey), 0, listOperations.size(buildKey(redisKey)));
        return list;
    }

    public Object rightPop(final String redisKey) {
        ListOperations<Serializable, Object> listOperations = redisTemplate.opsForList();
        return listOperations.rightPop(buildKey(redisKey));
    }

    public RedisTemplate<Serializable, Object> getRedisTemplate() {
        return redisTemplate;
    }

    /**
     * 注意，每个微服务初始化时固定绑定一个DB，不要切换DB。
     * 因为redisTemplate是静态的切换DB可能会错误先后覆盖
     *
     * @param dbIndex
     */
    private void changeDB(int dbIndex) {
        LettuceConnectionFactory jedisConnectionFactory = (LettuceConnectionFactory) redisTemplate.getConnectionFactory();
        jedisConnectionFactory.setDatabase(dbIndex);
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        jedisConnectionFactory.resetConnection();
    }

}
