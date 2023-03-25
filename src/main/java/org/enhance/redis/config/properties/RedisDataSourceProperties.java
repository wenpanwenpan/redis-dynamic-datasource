package org.enhance.redis.config.properties;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * redis多数据源配置，缓存每个数据源的配置信息到map中
 *
 * @author Mr_wenpan@163.com 2021/8/31 9:10 上午
 */
@ConfigurationProperties(prefix = RedisDataSourceProperties.PREFIX)
public class RedisDataSourceProperties {

    static final String PREFIX = "spring.redis";

    /**
     * 是否开启动态数据库切换 默认开启，如果关闭需要在yml中配置spring.redis.dynamic-database=false
     */
    private boolean dynamicDatabase = true;

    /**
     * 数据源配置集合（key: 数据源名称， value: 数据源对应的Redis配置）
     */
    private Map<String, RedisProperties> datasource = new ConcurrentHashMap<>();

    public Map<String, RedisProperties> getDatasource() {
        return datasource;
    }

    public void setDatasource(Map<String, RedisProperties> datasource) {
        this.datasource = datasource;
    }

    /**
     * 新增数据源配置
     *
     * @param datasourceName  Redis数据源名称
     * @param redisProperties Redis配置
     */
    public void addRedisProperties(String datasourceName, RedisProperties redisProperties) {
        if (StringUtils.isBlank(datasourceName)) {
            throw new IllegalArgumentException("datasource name can not be null, please check.");
        }
        if (Objects.isNull(redisProperties)) {
            throw new IllegalArgumentException("redisProperties can not be null, please check.");
        }
        datasource.put(datasourceName, redisProperties);
    }

    /**
     * 根据数据源名称获取Redis数据源配置
     *
     * @param datasourceName 数据源名称
     * @return org.springframework.boot.autoconfigure.data.redis.RedisProperties
     * @author wenpan 2022/9/24 5:54 下午
     */
    public RedisProperties getRedisProperties(String datasourceName) {
        return datasource.get(datasourceName);
    }

    public boolean isDynamicDatabase() {
        return dynamicDatabase;
    }

    public boolean getDynamicDatabase() {
        return dynamicDatabase;
    }

}