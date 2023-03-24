package org.enhance.redis.runner;

import org.enhance.redis.helper.ApplicationContextHelper;
import org.enhance.redis.helper.RedisHelper;
import org.enhance.redis.infra.constant.DynamicRedisConstants;
import org.enhance.redis.infra.util.EnvironmentUtil;
import org.enhance.redis.register.RedisDataSourceRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;

import java.util.Set;

/**
 * Redis多数据源注册runner
 * 一个数据源对应一个RedisHelper，一个redisHelper（动态）中包含着多个RedisTemplate（每个db一个RedisTemplate）
 * 该runner的作用是，当容器启动完毕后，将容器中所有的多数据源的RedisHelper和RedisTemplate缓存起来
 *
 * @author Mr_wenpan@163.com 2021/09/06 10:33
 */
public class RedisMultiSourceRegisterRunner implements CommandLineRunner, EnvironmentAware {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Environment environment;

    @Override
    @SuppressWarnings("rawtypes")
    public void run(String... args) {
        // 获取所有数据源名称（注意：只包含spring.redis.datasource下的数据源）
        Set<String> dataSourceNames = EnvironmentUtil.loadRedisDataSourceName((AbstractEnvironment) environment);

        if (dataSourceNames.size() < 1) {
            logger.error("no multi datasource config, register multi datasource failed. please check config.");
            return;
        }

        // 注册默认数据源的redisHelper和redisTemplate(这两个bean在自动配置类中已经注入了)
        RedisHelper defaultRedisHelper = ApplicationContextHelper.getContext()
                .getBean(DynamicRedisConstants.DefaultRedisHelperName.REDIS_HELPER, RedisHelper.class);
        RedisTemplate defaultRedisTemplate = ApplicationContextHelper.getContext()
                .getBean(DynamicRedisConstants.DefaultRedisTemplateName.REDIS_TEMPLATE, RedisTemplate.class);
        RedisDataSourceRegister.registerRedisHelper(DynamicRedisConstants.MultiSource.DEFAULT_SOURCE_HELPER, defaultRedisHelper);
        RedisDataSourceRegister.registerRedisTemplate(DynamicRedisConstants.MultiSource.DEFAULT_SOURCE_TEMPLATE, defaultRedisTemplate);

        // 注册多数据源的RedisHelper
        dataSourceNames.forEach(name -> {
            String realTemplateName = name + DynamicRedisConstants.MultiSource.REDIS_TEMPLATE;
            String realHelperName = name + DynamicRedisConstants.MultiSource.REDIS_HELPER;
            // 通过数据源名称获取bean
            RedisTemplate redisTemplate = ApplicationContextHelper.getContext().getBean(realTemplateName, RedisTemplate.class);
            // 如果开启动态切换db则创建动态redisHelper，反之则创建静态redisHelper
            RedisHelper redisHelper = ApplicationContextHelper.getContext().getBean(realHelperName, RedisHelper.class);
            // 注册RedisTemplate
            RedisDataSourceRegister.registerRedisTemplate(realTemplateName, redisTemplate);
            // 注册RedisHelper
            RedisDataSourceRegister.registerRedisHelper(realHelperName, redisHelper);
        });

    }

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }
}
