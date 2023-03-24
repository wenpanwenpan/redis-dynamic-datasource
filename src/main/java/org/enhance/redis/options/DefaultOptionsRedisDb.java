package org.enhance.redis.options;

import org.enhance.redis.helper.RedisHelper;
import org.enhance.redis.template.DynamicRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;

/**
 * 操作redis db默认实现
 *
 * @author Mr_wenpan@163.com 2021/09/06 13:58
 */
public class DefaultOptionsRedisDb extends AbstractOptionsRedisDb<String, String> {

    public DefaultOptionsRedisDb(RedisHelper redisHelper) {
        super(redisHelper);
    }

    @Override
    public RedisTemplate<String, String> opsDbZero() {
        return commonOpsDb(0);
    }

    @Override
    public RedisTemplate<String, String> opsDbOne() {
        return commonOpsDb(1);
    }

    @Override
    public RedisTemplate<String, String> opsDbTwo() {
        return commonOpsDb(2);
    }

    @Override
    public RedisTemplate<String, String> opsDbThree() {
        return commonOpsDb(3);
    }

    @Override
    public RedisTemplate<String, String> opsDbFour() {
        return commonOpsDb(4);
    }

    @Override
    public RedisTemplate<String, String> opsDbFive() {
        return commonOpsDb(5);
    }

    @Override
    public RedisTemplate<String, String> opsDbSix() {
        return commonOpsDb(6);
    }

    @Override
    public RedisTemplate<String, String> opsDbSeven() {
        return commonOpsDb(7);
    }

    @Override
    public RedisTemplate<String, String> opsDbEight() {
        return commonOpsDb(8);
    }

    @Override
    public RedisTemplate<String, String> opsDbNine() {
        return commonOpsDb(9);
    }

    @Override
    public RedisTemplate<String, String> opsDbTen() {
        return commonOpsDb(10);
    }

    @Override
    public RedisTemplate<String, String> opsDbEleven() {
        return commonOpsDb(11);
    }

    @Override
    public RedisTemplate<String, String> opsDbTwelve() {
        return commonOpsDb(12);
    }

    @Override
    public RedisTemplate<String, String> opsDbThirteen() {
        return commonOpsDb(13);
    }

    @Override
    public RedisTemplate<String, String> opsDbFourteen() {
        return commonOpsDb(14);
    }

    @Override
    public RedisTemplate<String, String> opsDbFifteen() {
        return commonOpsDb(15);
    }

    @Override
    public RedisTemplate<String, String> opsOtherDb(int db) {

        return commonOpsDb(db);
    }

    /**
     * 操作db公用方法
     */
    private RedisTemplate<String, String> commonOpsDb(int db) {
        // 获取到该redisHelper对应的Redis数据源操作的redisTemplates
        Map<Object, RedisTemplate<String, String>> redisTemplates = redisHelper.getRedisTemplates();
        // 静态redisHelper不能切换db
        if (redisTemplates == null) {
            throw new RuntimeException("静态redisHelper不支持动态切换redis db，若需要动态切换db，请开启动态配置.");
        }

        // 获取到该RedisHelper的redisTemplate(一定有，在创建redisHelper的时候就赋值了)
        DynamicRedisTemplate<String, String> dynamicRedisTemplate = (DynamicRedisTemplate<String, String>) redisHelper.getRedisTemplate();
        RedisTemplate<String, String> redisTemplate = redisTemplates.get(db);

        // 双重检查，这里直接使用synchronized锁，因为创建redisTemplate不会很频繁，一般整个生命周期只有几次，不会有性能问题
        if (null == redisTemplate) {
            synchronized (DynamicRedisTemplate.class) {
                if (null == redisTemplates.get(db)) {
                    // 创建到该db的RedisTemplate并缓存起来
                    RedisTemplate<String, String> redisTemplateOnMissing = dynamicRedisTemplate.createRedisTemplateOnMissing(db);
                    redisTemplates.put(db, redisTemplateOnMissing);
                }
            }
        }

        return redisTemplates.get(db);
    }

}
