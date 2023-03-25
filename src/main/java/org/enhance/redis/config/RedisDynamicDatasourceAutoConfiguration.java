package org.enhance.redis.config;

import org.enhance.redis.annotation.EnableRedisMultiDataSource;
import org.enhance.redis.RedisMultiDataSourceRegistrar;
import org.enhance.redis.client.RedisMultiSourceClient;
import org.enhance.redis.config.properties.RedisDataSourceProperties;
import org.enhance.redis.config.properties.DynamicRedisProperties;
import org.enhance.redis.helper.ApplicationContextHelper;
import org.enhance.redis.helper.DynamicRedisHelper;
import org.enhance.redis.helper.RedisHelper;
import org.enhance.redis.infra.condition.ConditionalOnExistingProperty;
import org.enhance.redis.infra.condition.ConditionalOnMissingProperty;
import org.enhance.redis.template.DynamicRedisTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.lang.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.enhance.redis.infra.constant.DynamicRedisConstants.DefaultRedisHelperName;
/**
 * redis动态数据源自动配置
 *
 * @author wenpan 2023/03/24 21:51
 */
@Configuration
@ComponentScan({"org.enhance.redis.client"})
@EnableConfigurationProperties({DynamicRedisProperties.class, RedisDataSourceProperties.class})
@ConditionalOnClass(name = {"org.springframework.data.redis.connection.RedisConnectionFactory"})
public class RedisDynamicDatasourceAutoConfiguration {

    /**
     * 导入springboot自动配置中导入的RedisTemplate和StringRedisTemplate
     * 该配置导入后下面的RedisTemplate和StringRedisTemplate就无法注入了
     */
    /*@Import(RedisAutoConfiguration.class)
    @Configuration
    static class ImportRedisAutoConfiguration {

    }*/

    @Bean
    public ApplicationContextHelper applicationContextHelper() {
        return new ApplicationContextHelper();
    }

    /**
     * 注入RedisTemplate，key-value都使用string类型
     * RedisConnectionFactory由对应的spring-boot-autoconfigure自动配置到容器
     */
    @Bean
    @ConditionalOnMissingBean(RedisTemplate.class)
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        buildRedisTemplate(redisTemplate, redisConnectionFactory);
        return redisTemplate;
    }

    /**
     * 注入StringRedisTemplate，key-value序列化器都使用String序列化器
     */
    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        buildRedisTemplate(redisTemplate, redisConnectionFactory);
        return redisTemplate;
    }

    /**
     * 通过Redis连接工厂构建一个RedisTemplate
     */
    private static void buildRedisTemplate(RedisTemplate<String, String> redisTemplate,
                                           RedisConnectionFactory redisConnectionFactory) {
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setStringSerializer(stringRedisSerializer);
        redisTemplate.setDefaultSerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(stringRedisSerializer);
        redisTemplate.setConnectionFactory(redisConnectionFactory);
    }

    /**
     * <p>
     * 注入RedisMultiDataSourceRegistrar，必须要有{@link RedisMultiSourceClient 时才注入}
     * 注意：{@link EnableRedisMultiDataSource} 中虽然有使用@Import注解向容器中导入
     * {@link RedisMultiDataSourceRegistrar} 类，但是由于 RedisMultiDataSourceRegistrar 实现了
     * {@link ImportBeanDefinitionRegistrar} 接口导致spring并不会将 RedisMultiDataSourceRegistrar 注入容器，所以需要在这里手动注入
     * </p>
     */
    @Bean
    @ConditionalOnBean(value = {RedisMultiSourceClient.class})
    @ConditionalOnMissingBean(value = RedisMultiDataSourceRegistrar.class)
    public RedisMultiDataSourceRegistrar redisMultiDataSourceRegistrar(Environment environment) {
        RedisMultiDataSourceRegistrar redisMultiDataSourceRegistrar = new RedisMultiDataSourceRegistrar();
        redisMultiDataSourceRegistrar.setEnvironment(environment);
        return redisMultiDataSourceRegistrar;
    }

    /**
     * 默认数据源的普通 RedisHelper，关闭动态数据库切换或集群模式下时才注入（redis官方要求集群模式下不能切换db只有db0）
     */
    @Primary
    @ConditionalOnStaticRedisHelper
    @Bean(name = {"redisHelper", DefaultRedisHelperName.DEFAULT, DefaultRedisHelperName.DEFAULT_REDIS_HELPER})
    public RedisHelper redisHelper(RedisTemplate<String, String> redisTemplate) {
        return new RedisHelper(redisTemplate);
    }

    /**
     * 默认数据源的动态redisHelper
     * 这些参数由 jedis 或 lettuce 客户端帮我们自动配置并注入到容器
     */
    @Primary
    @ConditionalOnDynamicRedisHelper
    @Bean(name = {"redisHelper", DefaultRedisHelperName.DEFAULT, DefaultRedisHelperName.DEFAULT_REDIS_HELPER})
    public RedisHelper dynamicRedisHelper(StringRedisTemplate redisTemplate,
                                          RedisProperties redisProperties,
                                          ObjectProvider<RedisSentinelConfiguration> sentinelConfiguration,
                                          ObjectProvider<RedisClusterConfiguration> clusterConfiguration,
                                          ObjectProvider<List<JedisClientConfigurationBuilderCustomizer>> jedisBuilderCustomizers,
                                          ObjectProvider<List<LettuceClientConfigurationBuilderCustomizer>> builderCustomizers) {
        // 构建动态RedisTemplate工厂
        DynamicRedisTemplateFactory<String, String> dynamicRedisTemplateFactory =
                new DynamicRedisTemplateFactory<>(redisProperties,
                        sentinelConfiguration.getIfAvailable(),
                        clusterConfiguration.getIfAvailable(),
                        jedisBuilderCustomizers.getIfAvailable(),
                        builderCustomizers.getIfAvailable());
        // ======================================================================================================
        // 这里在注入的时候默认值注入一个默认的redisTemplate，以及将这个redisTemplate放入到map中，该redisTemplate
        // 操作的是配置文件中使用spring.redis.database属性指定的db（若不显示指定，则使用的0号db）
        // 注入的时候值放入这个默认的的redisTemplate到map中，在使用的时候如果需要动态切换库那么会通过连接工厂
        // 重新去创建一个redisTemplate，并缓存到map中（懒加载模式），并不会一次性创建出多个redisTemplate然后缓存起来（连接很昂贵）
        // ======================================================================================================

        DynamicRedisTemplate<String, String> dynamicRedisTemplate = new DynamicRedisTemplate<>(dynamicRedisTemplateFactory);
        // 当不指定库时，默认使用的RedisTemplate来操作Redis(直接获取容器中的)
        dynamicRedisTemplate.setDefaultRedisTemplate(redisTemplate);
        Map<Object, RedisTemplate<String, String>> map = new HashMap<>(8);
        // 配置文件中指定使用几号db
        map.put(redisProperties.getDatabase(), redisTemplate);
        // 将redisTemplate缓存起来
        dynamicRedisTemplate.setRedisTemplates(map);

        return new DynamicRedisHelper(dynamicRedisTemplate);
    }


    /**
     * @return Hash 处理类
     */
    @Bean
    public HashOperations<String, String, String> hashOperations(StringRedisTemplate redisTemplate) {
        return redisTemplate.opsForHash();
    }

    /**
     * @return String 处理类
     */
    @Bean
    public ValueOperations<String, String> valueOperations(StringRedisTemplate redisTemplate) {
        return redisTemplate.opsForValue();
    }

    /**
     * @return List 处理类
     */
    @Bean
    public ListOperations<String, String> listOperations(StringRedisTemplate redisTemplate) {
        return redisTemplate.opsForList();
    }

    /**
     * @return Set 处理类
     */
    @Bean
    public SetOperations<String, String> setOperations(StringRedisTemplate redisTemplate) {
        return redisTemplate.opsForSet();
    }

    /**
     * @return ZSet 处理类
     */
    @Bean
    public ZSetOperations<String, String> zSetOperations(StringRedisTemplate redisTemplate) {
        return redisTemplate.opsForZSet();
    }

    @Documented
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Conditional(OnDynamicRedisHelperCondition.class)
    @interface ConditionalOnDynamicRedisHelper {
    }

    /**
     * dynamic.redis.dynamic-database=true 且 非集群模式下，则创建动态的 RedisHelper，可以切换 Redis DB.
     */
    private static class OnDynamicRedisHelperCondition extends AllNestedConditions {

        /**
         * 注册bean时生效
         */
        public OnDynamicRedisHelperCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        /**
         * 开启db动态切换
         */
        @ConditionalOnProperty(prefix = DynamicRedisProperties.PREFIX,
                name = "dynamic-database", havingValue = "true", matchIfMissing = true)
        static class OnDynamicRedisHelper {
        }

        /**
         * 非集群配置
         */
        @ConditionalOnMissingProperty("spring.redis.cluster.nodes")
        static class OnSingleCluster {
        }
    }

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Conditional(OnStaticRedisHelperCondition.class)
    @interface ConditionalOnStaticRedisHelper {

    }

    /**
     * dynamic.redis.dynamic-database=false或者集群模式下，则创建静态的 RedisHelper，禁止切换 Redis db
     */
    private static class OnStaticRedisHelperCondition extends AnyNestedCondition {

        public OnStaticRedisHelperCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        /**
         * 关闭db动态切换
         */
        @ConditionalOnProperty(prefix = DynamicRedisProperties.PREFIX, name = "dynamic-database", havingValue = "false")
        static class OnStaticRedisHelper {
        }

        /**
         * 非集群模式
         * 存在spring.redis.cluster.nodes配置(即集群模式禁止切换db，Redis官方规定集群模式下只有db0)
         */
        @ConditionalOnExistingProperty(value = {"spring.redis.cluster.nodes"})
        static class OnRedisCluster {
        }
    }
}
