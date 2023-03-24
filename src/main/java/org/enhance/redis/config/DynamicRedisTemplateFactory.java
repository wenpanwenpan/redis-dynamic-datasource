package org.enhance.redis.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 动态 RedisTemplate 工厂类，用于创建和管理RedisTemplate
 * 为什么放这个包下？因为根据源码得知jedis、lettuce这些连接配置类都是default的访问权限，说明这些配置并不想被其他类访问到
 *
 * @author Mr_wenpan@163.com 2021/8/3 11:04 下午
 */
public class DynamicRedisTemplateFactory<K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicRedisTemplateFactory.class);

    // ==============================================================================================================
    // 从data-redis源码得知，构建jedis客户端配置（JedisConnectionConfigure）或lettuce客户端配置（LettuceConnectionConfigure）
    // 需要如下参数，并且从spring自动配置模块的源码可以看到，这些属性会由springboot自动配置帮我们注入到容器中，所以这里可以通过构造器
    // 将这些属性传递进来，并保存到属性上以便后面使用。经测试没有任何问题
    // ==============================================================================================================

    /**
     * Redis配置信息
     */
    private final RedisProperties properties;
    /**
     * redis 哨兵配置
     */
    private final RedisSentinelConfiguration sentinelConfiguration;
    /**
     * redis 集群配置
     */
    private final RedisClusterConfiguration clusterConfiguration;
    /**
     * jedis配置定制
     */
    private final List<JedisClientConfigurationBuilderCustomizer> jedisBuilderCustomizers;
    /**
     * lettuce配置定制
     */
    private final List<LettuceClientConfigurationBuilderCustomizer> lettuceBuilderCustomizers;

    private static final String REDIS_CLIENT_LETTUCE = "lettuce";
    private static final String REDIS_CLIENT_JEDIS = "jedis";

    /**
     * 这些参数由springboot自动配置帮我们自动配置并注入到容器
     * ObjectProvider更加宽松的依赖注入
     */
    public DynamicRedisTemplateFactory(RedisProperties properties,
                                       RedisSentinelConfiguration sentinelConfiguration,
                                       RedisClusterConfiguration clusterConfiguration,
                                       List<JedisClientConfigurationBuilderCustomizer> jedisBuilderCustomizers,
                                       List<LettuceClientConfigurationBuilderCustomizer> lettuceBuilderCustomizers) {
        this.properties = properties;
        this.sentinelConfiguration = sentinelConfiguration;
        this.clusterConfiguration = clusterConfiguration;
        this.jedisBuilderCustomizers = jedisBuilderCustomizers;
        this.lettuceBuilderCustomizers = lettuceBuilderCustomizers;
    }

    /**
     * 为指定的db创建RedisTemplate，用于操作Redis
     *
     * @param database redis db
     * @return org.springframework.data.redis.core.RedisTemplate<K, V>
     * @author Mr_wenpan@163.com 2021/8/7 1:47 下午
     */
    public RedisTemplate<K, V> createRedisTemplate(int database) {
        RedisConnectionFactory redisConnectionFactory = null;
        // 根据Redis客户端类型创建Redis连接工厂（用于创建RedisTemplate）
        switch (getRedisClientType()) {
            case REDIS_CLIENT_LETTUCE:
                // 使用指定的db创建lettuce redis连接工厂(创建方式参照源码：LettuceConnectionConfiguration)
                LettuceConnectionConfigure lettuceConnectionConfigure = new LettuceConnectionConfigure(
                        properties, sentinelConfiguration, clusterConfiguration, lettuceBuilderCustomizers, database);
                redisConnectionFactory = lettuceConnectionConfigure.redisConnectionFactory();
                break;
            case REDIS_CLIENT_JEDIS:
                // 使用指定的db创建jedis redis连接工厂（创建方式参照源码：JedisConnectionConfiguration）
                JedisConnectionConfigure jedisConnectionConfigure = new JedisConnectionConfigure(properties,
                        sentinelConfiguration, clusterConfiguration, jedisBuilderCustomizers, database);
                redisConnectionFactory = jedisConnectionConfigure.redisConnectionFactory();
                break;
            default:
                LOGGER.error("unknow redis client type.");
        }
        Assert.notNull(redisConnectionFactory, "redisConnectionFactory is null.");
        // 通过Redis连接工厂创建RedisTemplate
        return createRedisTemplate(redisConnectionFactory);
    }

    /**
     * 通过Redis连接工厂来创建一个redisTemplate用于操作Redis db
     */
    private RedisTemplate<K, V> createRedisTemplate(RedisConnectionFactory factory) {
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        RedisTemplate<K, V> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setStringSerializer(stringRedisSerializer);
        redisTemplate.setDefaultSerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(stringRedisSerializer);
        // 设置Redis连接工厂用于创建连接
        redisTemplate.setConnectionFactory(factory);
        // 调用afterPropertiesSet方法，在属性设置完成后做一些检查和额外工作
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 获取Redis客户端的类型，提供jedis和lettuce两种
     */
    private static String getRedisClientType() {
        try {
            // 如果能加载lettuce，则优先使用lettuce
            Class.forName("io.lettuce.core.RedisClient");
            return REDIS_CLIENT_LETTUCE;
        } catch (ClassNotFoundException e) {
            LOGGER.debug("Not Lettuce redis client");
        }

        try {
            // 如果能加载Jedis，则使用Jedis
            Class.forName("redis.clients.jedis.Jedis");
            return REDIS_CLIENT_JEDIS;
        } catch (ClassNotFoundException e) {
            LOGGER.debug("Not Jedis redis client");
        }

        throw new RuntimeException("redis client not found.");
    }

}