package org.enhance.redis.annotation;

import org.enhance.redis.RedisMultiDataSourceRegistrar;
import org.enhance.redis.client.RedisMultiSourceClient;
import org.enhance.redis.runner.RedisMultiSourceRegisterRunner;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启Redis多数据源，由@EnableRedisMultiDataSource注解控制是否开启多数据源
 * 只有使用@EnableRedisMultiDataSource注解显示开启使用多数据源时才会注入相关的类
 * （比如：RedisMultiDataSourceRegistrar，RedisMultiSourceRegisterRunner，RedisMultisourceClient）
 *
 * @author Mr_wenpan@163.com 2021/8/24 9:33 下午
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({RedisMultiDataSourceRegistrar.class, RedisMultiSourceRegisterRunner.class, RedisMultiSourceClient.class})
public @interface EnableRedisMultiDataSource {

}