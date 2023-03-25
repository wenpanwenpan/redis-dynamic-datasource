package org.enhance.redis;

import org.enhance.redis.annotation.RedisDataSource;
import org.enhance.redis.config.DynamicRedisTemplateFactory;
import org.enhance.redis.config.properties.RedisDataSourceProperties;
import org.enhance.redis.helper.DynamicRedisHelper;
import org.enhance.redis.helper.RedisHelper;
import org.enhance.redis.infra.constant.DynamicRedisConstants;
import org.enhance.redis.infra.util.EnvironmentUtil;
import org.enhance.redis.template.DynamicRedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Redis数据源注册，根据 <code>spring.redis.datasource.[name]</code> 配置的数据源名称注册一个 {@link RedisTemplate} 和 {@link RedisHelper}.
 * <p>
 * RedisTemplate 的 bean 名称为 <i>nameRedisTemplate</i>，可以通过 {@link Qualifier} 根据名称注册
 * <p>
 * RedisHelper 的 bean 名称有两个： <i>name</i> 以及 <i>nameRedisHelper</i>，可以通过 {@link RedisDataSource} 注入
 *
 * @author wenpanfeng
 */
public class RedisMultiDataSourceRegistrar implements EnvironmentAware, ImportBeanDefinitionRegistrar {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Environment environment;

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }

    /**
     * 为每个redis数据源注入BeanDefinition
     */
    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata annotationMetadata,
                                        @NonNull BeanDefinitionRegistry registry) {

        Set<String> names = EnvironmentUtil.loadRedisDataSourceName((AbstractEnvironment) environment);

        if (names.size() <= 0) {
            logger.error("no multi datasource config, inject multi datasource failed. please check config.");
            return;
        }

        logger.info("register redis datasource: {}", names);

        for (String name : names) {
            // 注册 RedisTemplate BeanDefinition
            registerRedisTemplateBeanDefinition(name, RedisTemplateFactoryBean.class, registry);

            // 注册 RedisHelper BeanDefinition
            registerRedisHelperBeanDefinition(name, RedisHelperFactoryBean.class, registry);
        }
    }

    /**
     * 注册 RedisTemplate BeanDefinition
     */
    protected final void registerRedisTemplateBeanDefinition(String alias, Class<?> type, BeanDefinitionRegistry registry) {
        // BeanDefinition构建器
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(type);
        // 设置通过名称注入
        builder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_NAME);
        builder.addConstructorArgValue(null);
        // 设置数据源的名称(即设置bean的datasource属性的值)
        builder.addPropertyValue(RedisDataSourceContext.FIELD_DATASOURCE_NAME, alias);

        // 通过构建器获取bean的定义信息
        BeanDefinition beanDefinition = builder.getBeanDefinition();
        // 设置主要的注入的对象
        beanDefinition.setPrimary(false);

        String beanName = alias + DynamicRedisConstants.MultiSource.REDIS_TEMPLATE;
        // 设置该bean的名称（数据源名称 + RedisTemplate）和别名（数据源名称 + -template）
        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, beanName, new String[]{alias + "-template"});
        // 注册bean定义信息
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }

    /**
     * 注册 RedisHelper BeanDefinition
     */
    protected final void registerRedisHelperBeanDefinition(String alias, Class<?> type, BeanDefinitionRegistry registry) {
        // BeanDefinition构建器
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(type);
        // 设置通过名称注入
        builder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_NAME);
        builder.addConstructorArgValue(null);
        // 设置数据源的名称(即设置bean的datasource属性的值)
        builder.addPropertyValue(RedisDataSourceContext.FIELD_DATASOURCE_NAME, alias);

        // 通过构建器获取bean的定义信息
        BeanDefinition beanDefinition = builder.getBeanDefinition();
        beanDefinition.setPrimary(false);
        beanDefinition.setDependsOn(alias + DynamicRedisConstants.MultiSource.REDIS_TEMPLATE);

        String beanName = alias + DynamicRedisConstants.MultiSource.REDIS_HELPER;
        // 设置该bean的名称（数据源名称 + RedisHelper）和别名（数据源名称 + -helper）
        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, beanName, new String[]{alias, alias + "-helper"});
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }

    /**
     * 创建 RedisHelper 的 FactoryBean
     */
    @SuppressWarnings("all")
    protected class RedisHelperFactoryBean extends RedisDataSourceContext implements FactoryBean<Object> {

        private final Logger logger = LoggerFactory.getLogger(getClass());
        @Override
        public Object getObject() throws Exception {
            // 获取配置文件中的参数，通过判断是否开启动态切换db来创建redisHelper
            RedisDataSourceProperties dynamicRedisProperties = applicationContext.getBean(RedisDataSourceProperties.class);
            // 获取指定数据源对应的RedisTemplate
            RedisTemplate<String, String> redisTemplate = applicationContext.getBean(dataSourceName + "RedisTemplate", RedisTemplate.class);
            if (dynamicRedisProperties.getDynamicDatabase()) {
                // 为该数据源创建一个Redis连接工厂(连接到指定的数据源)
                DynamicRedisTemplateFactory<String, String> dynamicRedisTemplateFactory = getDynamicRedisTemplateFactory();
                DynamicRedisTemplate<String, String> dynamicRedisTemplate = new DynamicRedisTemplate<>(dynamicRedisTemplateFactory);
                // 将该数据源对应的默认RedisTemplate设置到动态dynamicRedisTemplate中
                dynamicRedisTemplate.setDefaultRedisTemplate(redisTemplate);
                Map<Object, RedisTemplate<String, String>> redisTemplateMap = new HashMap<>(8);
                redisTemplateMap.put(getRedisProperties().getDatabase(), redisTemplate);
                // 动态dynamicRedisTemplate保存多个RedisTemplate（对应该数据源的不同db）
                dynamicRedisTemplate.setRedisTemplates(redisTemplateMap);

                logger.info("create dynamic RedisHelper named {}", getDataSourceName());

                // 通过dynamicRedisTemplate创建一个DynamicRedisHelper
                return new DynamicRedisHelper(dynamicRedisTemplate);
            }

            logger.info("create static RedisHelper named {}", getDataSourceName());

            // 创建静态redisHelper
            return new RedisHelper(redisTemplate);
        }

        @Override
        public Class<?> getObjectType() {
            return RedisHelper.class;
        }
    }

    /**
     * 创建 RedisTemplate 的 FactoryBean
     * FactoryBean一般用于构建复杂的bean
     */
    protected class RedisTemplateFactoryBean extends RedisDataSourceContext implements FactoryBean<Object> {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        /**
         * 返回要创建的bean对象
         */
        @Override
        public Object getObject() throws Exception {
            // 为该数据源创建一个Redis连接工厂，连向指定的数据源
            DynamicRedisTemplateFactory<String, String> dynamicRedisTemplateFactory = getDynamicRedisTemplateFactory();

            logger.info("Dynamic create a RedisTemplate named {}", getDataSourceName());

            // 使用工厂类去创建RedisTemplate（该工厂为我们自定义的创建RedisTemplate的工厂类，使用自定义redis相关配置以及db）
            // getRedisProperties()表示获取到该数据源对应的Redis配置
            return dynamicRedisTemplateFactory.createRedisTemplate(getRedisProperties().getDatabase());
        }

        /**
         * 返回要创建的bean的类型
         */
        @Override
        public Class<?> getObjectType() {
            return RedisTemplate.class;
        }
    }

}