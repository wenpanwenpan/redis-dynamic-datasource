package org.enhance.redis.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions.Builder;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Lettuce.Cluster.Refresh;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Pool;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration.LettuceClientConfigurationBuilder;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Redis connection configuration using Lettuce.
 * 自定义改造lettuce连接配置
 *
 * @author Mark Paluch
 * @author Andy Wilkinson
 */
class LettuceConnectionConfigure extends RedisConnectionConfiguration {

    /**
     * redis配置properties
     */
    private final RedisProperties properties;

    /**
     * Lettuce客户端定制
     */
    private final List<LettuceClientConfigurationBuilderCustomizer> builderCustomizers;

    private final ClientResources clientResources;

    LettuceConnectionConfigure(RedisProperties properties,
                               RedisSentinelConfiguration sentinelConfigurationProvider,
                               RedisClusterConfiguration clusterConfigurationProvider,
                               List<LettuceClientConfigurationBuilderCustomizer> builderCustomizers,
                               int database) {
        super(properties, sentinelConfigurationProvider, clusterConfigurationProvider, database);
        this.properties = properties;
        this.builderCustomizers = Optional.ofNullable(builderCustomizers).orElse(new ArrayList<>());
        // 每次new LettuceConnectionConfiguration 都新建一个clientResources，也可以使用容器中默认注入的
        // 参考 LettuceConnectionConfiguration
        clientResources = DefaultClientResources.create();
    }

    /**
     * 创建lettuce连接工厂
     */
    LettuceConnectionFactory redisConnectionFactory() {
        // 获取lettuce连接工厂配置信息
        LettuceClientConfiguration clientConfig = getLettuceClientConfiguration(clientResources, properties.getLettuce().getPool());
        // 创建lettuce连接工厂
        return createLettuceConnectionFactory(clientConfig);
    }

    /**
     * 创建lettuce连接工厂
     */
    private LettuceConnectionFactory createLettuceConnectionFactory(LettuceClientConfiguration clientConfiguration) {
        LettuceConnectionFactory lettuceConnectionFactory;
        if (getSentinelConfig() != null) {
            lettuceConnectionFactory = new LettuceConnectionFactory(getSentinelConfig(), clientConfiguration);
        } else if (getClusterConfiguration() != null) {
            lettuceConnectionFactory = new LettuceConnectionFactory(getClusterConfiguration(), clientConfiguration);
        } else {
            lettuceConnectionFactory = new LettuceConnectionFactory(getStandaloneConfig(), clientConfiguration);
        }
        // 由于我们手动创建lettuceConnectionFactory连接工厂，所以afterPropertiesSet并不会像spring一样自动被吊起
        // 必须手动调用afterPropertiesSet()，初始化connectionProvider，不然创建连接会报错空指针
        lettuceConnectionFactory.afterPropertiesSet();
        return lettuceConnectionFactory;
    }

    /**
     * 获取lettuce客户端配置
     */
    private LettuceClientConfiguration getLettuceClientConfiguration(ClientResources clientResources, Pool pool) {
        LettuceClientConfigurationBuilder builder = createBuilder(pool);
        applyProperties(builder);
        if (StringUtils.hasText(getProperties().getUrl())) {
            customizeConfigurationFromUrl(builder);
        }
        builder.clientOptions(createClientOptions());
        builder.clientResources(clientResources);
        customize(builder);
        return builder.build();
    }

    /**
     * 变更源码排序
     */
    private void customize(LettuceClientConfigurationBuilder builder) {
        for (LettuceClientConfigurationBuilderCustomizer customizer : builderCustomizers) {
            customizer.customize(builder);
        }
    }

    /**
     * 创建客户端配置构建器，用于构建客户端配置
     */
    private static LettuceClientConfigurationBuilder createBuilder(Pool pool) {
        if (pool == null) {
            return LettuceClientConfiguration.builder();
        }
        return PoolBuilderFactory.createBuilder(pool);
    }

    private LettuceClientConfigurationBuilder applyProperties(
            LettuceClientConfigurationBuilder builder) {
        if (getProperties().isSsl()) {
            builder.useSsl();
        }
        if (getProperties().getTimeout() != null) {
            builder.commandTimeout(getProperties().getTimeout());
        }
        if (getProperties().getLettuce() != null) {
            RedisProperties.Lettuce lettuce = getProperties().getLettuce();
            if (lettuce.getShutdownTimeout() != null && !lettuce.getShutdownTimeout().isZero()) {
                builder.shutdownTimeout(getProperties().getLettuce().getShutdownTimeout());
            }
        }
        if (StringUtils.hasText(getProperties().getClientName())) {
            builder.clientName(getProperties().getClientName());
        }
        return builder;
    }

    private ClientOptions createClientOptions() {
        ClientOptions.Builder builder = initializeClientOptionsBuilder();
        Duration connectTimeout = getProperties().getConnectTimeout();
        if (connectTimeout != null) {
            builder.socketOptions(SocketOptions.builder().connectTimeout(connectTimeout).build());
        }
        return builder.timeoutOptions(TimeoutOptions.enabled()).build();
    }

    private ClientOptions.Builder initializeClientOptionsBuilder() {
        if (getProperties().getCluster() != null) {
            ClusterClientOptions.Builder builder = ClusterClientOptions.builder();
            Refresh refreshProperties = getProperties().getLettuce().getCluster().getRefresh();
            Builder refreshBuilder = ClusterTopologyRefreshOptions.builder()
                    .dynamicRefreshSources(refreshProperties.isDynamicRefreshSources());
            if (refreshProperties.getPeriod() != null) {
                refreshBuilder.enablePeriodicRefresh(refreshProperties.getPeriod());
            }
            if (refreshProperties.isAdaptive()) {
                refreshBuilder.enableAllAdaptiveRefreshTriggers();
            }
            return builder.topologyRefreshOptions(refreshBuilder.build());
        }
        return ClientOptions.builder();
    }

    private void customizeConfigurationFromUrl(LettuceClientConfigurationBuilder builder) {
        ConnectionInfo connectionInfo = parseUrl(getProperties().getUrl());
        if (connectionInfo.isUseSsl()) {
            builder.useSsl();
        }
    }

    /**
     * Inner class to allow optional commons-pool2 dependency.
     */
    private static class PoolBuilderFactory {

        static LettuceClientConfigurationBuilder createBuilder(Pool properties) {
            return LettucePoolingClientConfiguration.builder().poolConfig(getPoolConfig(properties));
        }

        private static GenericObjectPoolConfig<?> getPoolConfig(Pool properties) {
            GenericObjectPoolConfig<?> config = new GenericObjectPoolConfig<>();
            config.setMaxTotal(properties.getMaxActive());
            config.setMaxIdle(properties.getMaxIdle());
            config.setMinIdle(properties.getMinIdle());
            if (properties.getTimeBetweenEvictionRuns() != null) {
                config.setTimeBetweenEvictionRunsMillis(properties.getTimeBetweenEvictionRuns().toMillis());
            }
            if (properties.getMaxWait() != null) {
                config.setMaxWaitMillis(properties.getMaxWait().toMillis());
            }
            return config;
        }

    }

}
