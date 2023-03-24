package org.enhance.redis.config;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.*;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base Redis connection configuration.
 * 参考：spring-boot-autoconfigure 下的 RedisConnectionConfiguration ，参考版本2.4.8
 * 为什么要拷贝出来呢？因为我们要自己手动的指定db，自己通过这些配置方法生产一个RedisTemplate
 *
 * @author Mark Paluch
 * @author Stephane Nicoll
 * @author Alen Turkovic
 * @author Scott Frederick
 */
public abstract class RedisConnectionConfiguration {

    /**
     * redis配置properties
     */
    private final RedisProperties properties;

    /**
     * redis 哨兵配置
     */
    private final RedisSentinelConfiguration sentinelConfiguration;

    /**
     * redis 集群配置
     */
    private final RedisClusterConfiguration clusterConfiguration;

    /**
     * redis所使用的库，为什么不直接从RedisProperties取？因为我们要为指定的db动态的创建RedisTemplate
     */
    private final int database;

    protected RedisConnectionConfiguration(RedisProperties properties,
                                           RedisSentinelConfiguration sentinelConfiguration,
                                           RedisClusterConfiguration clusterConfiguration,
                                           int database) {
        this.properties = properties;
        this.sentinelConfiguration = sentinelConfiguration;
        this.clusterConfiguration = clusterConfiguration;
        this.database = database;
    }

    /**
     * redis 单机模式配置信息
     */
    protected final RedisStandaloneConfiguration getStandaloneConfig() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        if (StringUtils.hasText(properties.getUrl())) {
            ConnectionInfo connectionInfo = parseUrl(properties.getUrl());
            config.setHostName(connectionInfo.getHostName());
            config.setPort(connectionInfo.getPort());
            config.setUsername(connectionInfo.getUsername());
            config.setPassword(RedisPassword.of(connectionInfo.getPassword()));
        } else {
            config.setHostName(properties.getHost());
            config.setPort(properties.getPort());
            config.setUsername(properties.getUsername());
            config.setPassword(RedisPassword.of(properties.getPassword()));
        }
        // 使用自定义db
        config.setDatabase(database);
        return config;
    }

    /**
     * redis哨兵模式配置信息
     */
    protected final RedisSentinelConfiguration getSentinelConfig() {
        if (sentinelConfiguration != null) {
            return sentinelConfiguration;
        }
        RedisProperties.Sentinel sentinelProperties = properties.getSentinel();
        if (sentinelProperties != null) {
            RedisSentinelConfiguration config = new RedisSentinelConfiguration();
            config.master(sentinelProperties.getMaster());
            config.setSentinels(createSentinels(sentinelProperties));
            config.setUsername(properties.getUsername());
            if (properties.getPassword() != null) {
                config.setPassword(RedisPassword.of(properties.getPassword()));
            }
            if (sentinelProperties.getPassword() != null) {
                config.setSentinelPassword(RedisPassword.of(sentinelProperties.getPassword()));
            }
            // 使用自定义db
            config.setDatabase(database);
            return config;
        }
        return null;
    }

    /**
     * redis集群配置
     * Create a {@link RedisClusterConfiguration} if necessary.
     *
     * @return {@literal null} if no cluster settings are set.
     */
    protected final RedisClusterConfiguration getClusterConfiguration() {
        // 如果已经有集群配置，则直接返回
        if (clusterConfiguration != null) {
            return clusterConfiguration;
        }
        if (properties.getCluster() == null) {
            return null;
        }
        RedisProperties.Cluster clusterProperties = properties.getCluster();
        RedisClusterConfiguration config = new RedisClusterConfiguration(clusterProperties.getNodes());
        if (clusterProperties.getMaxRedirects() != null) {
            // 最大跳转次数
            config.setMaxRedirects(clusterProperties.getMaxRedirects());
        }
        config.setUsername(properties.getUsername());
        // 密码设置
        if (properties.getPassword() != null) {
            config.setPassword(RedisPassword.of(properties.getPassword()));
        }
        return config;
    }

    protected final RedisProperties getProperties() {
        return properties;
    }

    /**
     * 创建Redis哨兵节点
     */
    private static List<RedisNode> createSentinels(RedisProperties.Sentinel sentinel) {
        List<RedisNode> nodes = new ArrayList<>();
        for (String node : sentinel.getNodes()) {
            try {
                String[] parts = StringUtils.split(node, ":");
                assert parts != null;
                Assert.state(parts.length == 2, "Must be defined as 'host:port'");
                nodes.add(new RedisNode(parts[0], Integer.parseInt(parts[1])));
            } catch (RuntimeException ex) {
                throw new IllegalStateException("Invalid redis sentinel property '" + node + "'", ex);
            }
        }
        return nodes;
    }

    /**
     * 解析Redis url连接，创建连接信息
     */
    protected static ConnectionInfo parseUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (!"redis".equals(scheme) && !"rediss".equals(scheme)) {
                // 无效的或畸形的url
                throw new RedisUrlSyntaxException(url);
            }
            boolean useSsl = ("rediss".equals(scheme));
            String username = null;
            String password = null;
            if (uri.getUserInfo() != null) {
                String candidate = uri.getUserInfo();
                int index = candidate.indexOf(':');
                if (index >= 0) {
                    username = candidate.substring(0, index);
                    password = candidate.substring(index + 1);
                } else {
                    password = candidate;
                }
            }
            return new ConnectionInfo(uri, useSsl, username, password);
        } catch (URISyntaxException ex) {
            // 无效的或畸形的url
            throw new RedisUrlSyntaxException("Malformed url '" + url + "'", ex);
        }
    }

    /**
     * Redis连接信息
     */
    static class ConnectionInfo {

        private final URI uri;

        private final boolean useSsl;

        private final String username;

        private final String password;

        ConnectionInfo(URI uri, boolean useSsl, String username, String password) {
            this.uri = uri;
            this.useSsl = useSsl;
            this.username = username;
            this.password = password;
        }

        boolean isUseSsl() {
            return useSsl;
        }

        String getHostName() {
            return uri.getHost();
        }

        int getPort() {
            return uri.getPort();
        }

        String getUsername() {
            return username;
        }

        String getPassword() {
            return password;
        }

    }

}
