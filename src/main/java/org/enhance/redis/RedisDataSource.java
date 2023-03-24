package org.enhance.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Redis 数据源，指定 Redis 数据源名称
 * <p>
 * 可以通过 spring.redis.datasource.<name>: 配置 Redis 多数据源
 *
 * @author Mr_wenpan@163.com 2021/8/31 11:16 上午
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Documented
@Qualifier
public @interface RedisDataSource {

    /**
     * 指定 Redis 数据源名称
     *
     * @return Redis 数据源名称
     */
    @AliasFor(annotation = Qualifier.class)
    String value();
}