package com.qujie.timedtask.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 * @date 2019/10/24 12:58
 */
@Component
public class RedisUtils {

    @Autowired
    public StringRedisTemplate redisTemplate;

    public static RedisUtils redisUtils;

    /**
     * 递增
     * @param key 键
    //     * @param by 要增加几(大于0)
     * @return
     */
    public long incr(String key, long delta){
        if(delta<0){
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }
    /**
     * 此方法只在 Spring启动时 加载一次
     */
    @PostConstruct
    public void init(){
        redisUtils=this;
        redisUtils.redisTemplate=this.redisTemplate;
    }

    /**
     *redis存入数据和设置缓存时间
     * @param key 键
     * @param value 值
     * @param time 秒
     */
    public void set(String key,String value,long time){
        redisUtils.redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }

    /**
     * redis存入数据
     * @param key 键
     * @param value 值
     */
    public void set(String key,String value){
        redisUtils.redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 根据 key 获取过期时间
     * @param key
     * @return
     */
    public Long getExpire(String key){
        return redisUtils.redisTemplate.getExpire(key,TimeUnit.SECONDS);
    }

    /**
     * 判断 key 是否存在
     * @param key
     * @return
     */
    public Boolean hasKey(String key){
        return redisUtils.redisTemplate.hasKey(key);
    }

    /**
     * 根据 key 设置过期时间
     * @param key key
     * @param time 秒
     * @return
     */
    public Boolean expire(String key,long time){
        return redisUtils.redisTemplate.expire(key,time , TimeUnit.SECONDS);
    }

    /**
     * 普通缓存获取
     * @param key 键
     * @return 值
     */
    public Object get(String key){
        Object value = null;
        try{
            value = key==null?null:redisTemplate.opsForValue().get(key);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
        }
        return value;
    }

    public Object del(String key){
        return redisTemplate.delete(key);
    }


}
