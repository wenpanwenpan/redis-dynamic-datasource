package org.enhance.redis;

import org.enhance.redis.client.RedisMultiSourceClient;
import org.enhance.redis.config.properties.RedisDataSourceProperties;
import org.enhance.redis.helper.ApplicationContextHelper;
import org.enhance.redis.helper.RedisHelper;
import org.enhance.redis.infra.constant.DynamicRedisConstants;
import org.enhance.redis.register.RedisDataSourceRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Objects;

/**
 * <p>
 * redis多数据源注册扩展点，通过该扩展点可动态的注入redis数据源到容器，
 * 注入后可以通过{@code datasourceName + RedisTemplate}
 * 从容器里获取新注册的redis数据源对应的 redisTemplate ，
 * 通过 {@code datasourceName + RedisHelper} 来获取新增数据源的 RedisHelper，
 * 当然也可以通过 {@link RedisMultiSourceClient#opsDefaultDb}
 * 或 {@link RedisMultiSourceClient#opsDbOne(String)} 等方式传入新增的数据源名称
 * 来对新增的数据源进行读写操作
 *     <ol>
 *         <li>支持自定义注册RedisTemplate</li>
 *         <li>支持自定义注册redisHelper</li>
 *     </ol>
 * </p>
 *
 * @author wenpanfeng 2022/09/23 16:41
 */
public class RedisMultiDataSourceRegistrarExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisMultiDataSourceRegistrarExtension.class);

    /**
     * <p>
     * 注册redis数据源，如果已经存在 datasourceName 的配置，则默认不覆盖，直接抛出异常
     * </p>
     *
     * @param datasourceName  数据源名称
     * @param redisProperties 该数据源对应的配置文件
     * @author wenpanfeng 2022/9/23 18:21
     */
    public static void registerRedisDataSource(String datasourceName, RedisProperties redisProperties) {

        registerRedisDataSource(datasourceName, redisProperties, false);
    }

    /**
     * <p>
     * 注册redis数据源，是否要覆盖已有配置由调用方决定
     * </p>
     *
     * @param datasourceName  数据源名称
     * @param redisProperties 该数据源对应的配置文件
     * @author wenpanfeng 2022/9/23 18:21
     */
    public static void registerRedisDataSource(String datasourceName, RedisProperties redisProperties, boolean allowOverwrite) {
        // 首先先向redis多数据源配置里添加配置
        addRedisDatasourceRedisProperties(datasourceName, redisProperties, allowOverwrite);

        // 新增 redisTemplate 并注入到容器，并且将 redisTemplate 注册到 {@link RedisDataSourceRegister}
        // 进行统一管理，供后续 {@link RedisDataSourceRegister} 使用
        registerRedisTemplate(datasourceName);

        // 新增redisHelper并注入到容器，并且将redisHelper注册到 {@link RedisDataSourceRegister}
        // 进行统一管理，供后续 {@link RedisDataSourceRegister} 使用
        registerRedisHelper(datasourceName);
    }

    /**
     * <p>
     * 动态注册Redis数据源的RedisTemplate
     * </p>
     *
     * @param datasourceName 数据源名称
     * @author wenpan 2022/9/24 6:03 下午
     */
    @SuppressWarnings("all")
    public static void registerRedisTemplate(String datasourceName) {
        // 获取spring工厂
        BeanDefinitionRegistry registry = ApplicationContextHelper.getSpringFactory();
        // 获取Redis多数据源注册器
        RedisMultiDataSourceRegistrar redisMultiDataSourceRegistrar =
                ApplicationContextHelper.getContext().getBean(RedisMultiDataSourceRegistrar.class);
        // 新增数据源的redisTemplate并注入到容器
        redisMultiDataSourceRegistrar.registerRedisTemplateBeanDefinition(
                datasourceName, RedisMultiDataSourceRegistrar.RedisTemplateFactoryBean.class, registry);

        // 注入到RedisDataSourceRegister进行统一管理，供后续 {@link RedisDataSourceRegister} 使用
        String redisTemplateName = datasourceName + DynamicRedisConstants.MultiSource.REDIS_TEMPLATE;
        RedisTemplate<String, String> redisTemplate =
                (RedisTemplate<String, String>) ApplicationContextHelper.getContext().getBean(redisTemplateName);
        RedisDataSourceRegister.registerRedisTemplate(redisTemplateName, redisTemplate);
    }

    /**
     * <p>
     * 动态注册Redis数据源的RedisHelper
     * </p>
     *
     * @param datasourceName 数据源名称
     * @author wenpan 2022/9/24 6:03 下午
     */
    public static void registerRedisHelper(String datasourceName) {
        // 获取spring工厂
        BeanDefinitionRegistry registry = ApplicationContextHelper.getSpringFactory();
        // 获取Redis多数据源注册器
        RedisMultiDataSourceRegistrar redisMultiDataSourceRegistrar =
                ApplicationContextHelper.getContext().getBean(RedisMultiDataSourceRegistrar.class);
        // 新增数据源对应的redisHelper并注入到容器
        redisMultiDataSourceRegistrar.registerRedisHelperBeanDefinition(
                datasourceName, RedisMultiDataSourceRegistrar.RedisHelperFactoryBean.class, registry);
        // 注入到RedisDataSourceRegister进行统一管理，供后续 {@link RedisDataSourceRegister} 使用
        String redisHelperName = datasourceName + DynamicRedisConstants.MultiSource.REDIS_HELPER;
        RedisHelper redisHelper = (RedisHelper) ApplicationContextHelper.getContext().getBean(redisHelperName);
        RedisDataSourceRegister.registerRedisHelper(redisHelperName, redisHelper);
    }

    /**
     * <p>
     * 新增Redis数据源配置
     * </p>
     *
     * @param redisProperties Redis数据源配置
     * @param allowOverwrite  是否允许覆盖
     * @author wenpan 2022/9/24 6:00 下午
     */
    public static void addRedisDatasourceRedisProperties(String datasourceName,
                                                         RedisProperties redisProperties,
                                                         boolean allowOverwrite) {
        RedisDataSourceProperties properties = ApplicationContextHelper.getContext().getBean(RedisDataSourceProperties.class);
        RedisProperties exists = properties.getRedisProperties(datasourceName);
        // 存在名称为datasourceName的Redis配置 且 不允许覆盖
        if (Objects.nonNull(exists) && !allowOverwrite) {
            LOGGER.warn("addRedisDatasourceRedisProperties failed, because RedisDataSourceProperties " +
                    "already exists datasource [{}] and allowOverwrite is false.", datasourceName);
            throw new IllegalArgumentException("addRedisDatasourceRedisProperties failed, can not overwrite " + datasourceName);
        }
        properties.addRedisProperties(datasourceName, redisProperties);
    }
}