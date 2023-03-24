package org.enhance.redis.strategy;

/**
 * 分片策略
 *
 * @author Mr_wenpan@163.com 2021/9/4 6:35 下午
 */
public interface RedisShardingStrategy<T> {

    /**
     * 根据分片条件获取分片redis实例编码
     *
     * @param strategyCondition 分片条件
     * @return redis sharding实例编码
     */
    String redisShardingInstance(T strategyCondition);
}