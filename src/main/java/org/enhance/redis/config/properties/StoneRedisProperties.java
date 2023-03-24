package org.enhance.redis.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RedisProperties
 *
 * @author Mr_wenpan@163.com 2021/8/7 3:13 下午
 */
@Data
@ConfigurationProperties(prefix = StoneRedisProperties.PREFIX)
public class StoneRedisProperties {

    public static final String PREFIX = "dynamic.redis";

    /**
     * 是否开启动态数据库切换 默认开启，如果关闭需要在yml中配置stone.redis.dynamic-database=false
     */
    private boolean dynamicDatabase = true;

    public boolean isDynamicDatabase() {
        return dynamicDatabase;
    }

    public boolean getDynamicDatabase() {
        return dynamicDatabase;
    }

}
