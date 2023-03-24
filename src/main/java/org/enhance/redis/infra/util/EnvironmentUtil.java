package org.enhance.redis.infra.util;

import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 环境工具
 *
 * @author Mr_wenpan@163.com 2021/09/06 17:15
 */
public class EnvironmentUtil {

    /**
     * 从环境信息Environment中解析出数据源的名称
     *
     * @return 数据源名称
     */
    public static Set<String> loadRedisDataSourceName(AbstractEnvironment environment) {
        MutablePropertySources propertySources = environment.getPropertySources();
        Set<String> configs = StreamSupport.stream(propertySources.spliterator(), false)
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
                .flatMap(Arrays::stream)
                .filter(propName -> propName.startsWith("spring.redis.datasource."))
                .collect(Collectors.toSet());

        if (configs.size() > 0) {
            return configs.stream().map(item -> item.split("\\.")[3]).collect(Collectors.toSet());
        }

        return Collections.emptySet();
    }
}
