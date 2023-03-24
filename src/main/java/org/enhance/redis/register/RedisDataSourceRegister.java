package org.enhance.redis.register;

import org.enhance.redis.helper.RedisHelper;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源注册
 *
 * @author Mr_wenpan@163.com 2021/09/06 10:30
 */
public class RedisDataSourceRegister {

    /**
     * 多数据源redisTemplate注册（不包括默认数据源）
     */
    private final static Map<String, RedisTemplate<String, String>> REDIS_TEMPLATE_REGISTER = new ConcurrentHashMap<>();

    /**
     * 多数据源redisHelper注册（不包括默认数据源）
     */
    private final static Map<String, RedisHelper> REDIS_HELPER_REGISTER = new ConcurrentHashMap<>();

    public RedisDataSourceRegister() {

    }

    /**
     * 注册RedisTemplate
     */
    public static void registerRedisTemplate(String name, RedisTemplate<String, String> redisTemplate) {
        if (redisTemplate == null || name == null) {
            return;
        }
        REDIS_TEMPLATE_REGISTER.put(name, redisTemplate);
    }

    /**
     * 注册RedisHelper
     */
    public static void registerRedisHelper(String name, RedisHelper redisHelper) {
        if (redisHelper == null || name == null) {
            return;
        }
        REDIS_HELPER_REGISTER.put(name, redisHelper);
    }

    /**
     * 获取指定数据源的RedisTemplate
     */
    public static RedisTemplate<String, String> getRedisTemplate(String name) {
        return REDIS_TEMPLATE_REGISTER.get(name);
    }

    /**
     * 获取指定数据源的RedisHelper
     */
    public static RedisHelper getRedisHelper(String name) {
        return REDIS_HELPER_REGISTER.get(name);
    }

    /**
     * 获取多数据源的RedisTemplate注册器
     */
    public static Map<String, RedisTemplate<String, String>> getRedisTemplateRegister() {
        return REDIS_TEMPLATE_REGISTER;
    }

    /**
     * 获取多数据源的RedisHelper注册器
     */
    public static Map<String, RedisHelper> getRedisHelperRegister() {
        return REDIS_HELPER_REGISTER;
    }

}
