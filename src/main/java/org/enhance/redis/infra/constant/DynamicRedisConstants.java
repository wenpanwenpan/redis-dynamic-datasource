package org.enhance.redis.infra.constant;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * data-redis增强模块常用常量
 *
 * @author Mr_wenpan@163.com 2021/08/25 15:57
 */
public interface DynamicRedisConstants {

    /**
     * 多数据源相关常量
     */
    interface MultiSource {

        String REDIS_TEMPLATE = "RedisTemplate";

        String REDIS_HELPER = "RedisHelper";

        String DEFAULT_SOURCE = "defaultSource";

        String DEFAULT_SOURCE_HELPER = "defaultSourceRedisHelper";

        String DEFAULT_SOURCE_TEMPLATE = "defaultSourceRedisTemplate";
    }

    /**
     * 默认redis数据源的redisHelper注入名称
     */
    interface DefaultRedisHelperName {

        String REDIS_HELPER = "redisHelper";

        String DEFAULT = "default";

        String DEFAULT_REDIS_HELPER = "default-helper";
    }

    /**
     * 默认redis数据源的 redisTemplate 注入名称
     */
    interface DefaultRedisTemplateName {

        String REDIS_TEMPLATE = "redisTemplate";

    }

    /**
     * redis script默认配置
     */
    interface RedisScript {
        /**
         * 通过传入的luaPath获取 DefaultRedisScript
         *
         * @param luaPath 脚本地址
         * @return DefaultRedisScript
         */
        static <T> DefaultRedisScript<T> getDefaultRedisScript(String luaPath) {
            ResourceScriptSource addQueueLua = new ResourceScriptSource(new ClassPathResource(luaPath));
            DefaultRedisScript<T> getRedisScript = new DefaultRedisScript<>();
            getRedisScript.setScriptSource(addQueueLua);
            return getRedisScript;
        }
    }

}
