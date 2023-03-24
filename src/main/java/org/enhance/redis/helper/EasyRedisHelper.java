package org.enhance.redis.helper;

import org.enhance.redis.infra.function.Execute;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 易用的redis操作帮助器，支持动态的切换db
 *
 * @author Mr_wenpan@163.com 2021/08/11 20:17
 */
public class EasyRedisHelper {

    private static RedisHelper redisHelper;

    static {
        final String redisHelper = "redisHelper";
        ApplicationContextHelper.asyncStaticSetter(RedisHelper.class, EasyRedisHelper.class, redisHelper);
    }

    /**
     * 设置当前线程中的db信息
     */
    public static void setCurrentDatabase(int db) {
        RedisDbThreadLocalHelper.set(db);
    }

    /**
     * 清除当前线程中的db信息
     */
    public static void clearCurrentDatabase() {
        RedisDbThreadLocalHelper.clear();
    }

    public static RedisTemplate<String, String> getRedisTemplate(int db) {
        setCurrentDatabase(db);
        return redisHelper.getRedisTemplate();
    }

    /**
     * 不带返回结果的redis操作
     *
     * @param db      redis db
     * @param execute 执行
     * @author Mr_wenpan@163.com 2021/8/11 9:28 下午
     */
    public static void execute(int db, Execute execute) {
        try {
            redisHelper.setCurrentDatabase(db);
            execute.execute();
        } finally {
            redisHelper.clearCurrentDatabase();
        }
    }

    /**
     * 不带返回结果的redis操作
     *
     * @param db          redis db
     * @param redisHelper 自定义redisHelper
     * @param execute     执行
     * @author Mr_wenpan@163.com 2021/8/11 10:03 下午
     */
    public static void execute(int db, RedisHelper redisHelper, Execute execute) {
        try {
            redisHelper.setCurrentDatabase(db);
            execute.execute();
        } finally {
            redisHelper.clearCurrentDatabase();
        }
    }

    /**
     * 不带返回结果的redis操作
     *
     * @param db          redis db
     * @param redisHelper 自定义redisHelper
     * @param consumer    消费者
     * @author Mr_wenpan@163.com 2021/8/11 10:03 下午
     */
    public static void execute(int db, RedisHelper redisHelper, Consumer<RedisHelper> consumer) {
        try {
            redisHelper.setCurrentDatabase(db);
            consumer.accept(redisHelper);
        } finally {
            redisHelper.clearCurrentDatabase();
        }
    }

    /**
     * 不带返回结果的redis操作
     *
     * @param db       redis db
     * @param consumer 消费型接口
     * @author Mr_wenpan@163.com 2021/8/12 3:16 下午
     */
    public static void execute(int db, Consumer<RedisTemplate<String, String>> consumer) {
        try {
            redisHelper.setCurrentDatabase(db);
            consumer.accept(redisHelper.getRedisTemplate());
        } finally {
            redisHelper.clearCurrentDatabase();
        }
    }

    /**
     * 带返回结果的redis操作
     *
     * @param db       redis db
     * @param supplier 供应型接口
     * @return T 返回值
     * @author Mr_wenpan@163.com 2021/8/11 9:28 下午
     */
    public static <T> T executeWithResult(int db, Supplier<T> supplier) {
        try {
            redisHelper.setCurrentDatabase(db);
            return supplier.get();
        } finally {
            redisHelper.clearCurrentDatabase();
        }
    }

    /**
     * 带返回结果的redis操作
     *
     * @param db          redis db
     * @param redisHelper 自定义redisHelper
     * @param supplier    供应型接口
     * @return T 返回值
     * @author Mr_wenpan@163.com 2021/8/11 10:20 下午
     */
    public static <T> T executeWithResult(int db, RedisHelper redisHelper, Supplier<T> supplier) {
        try {
            redisHelper.setCurrentDatabase(db);
            return supplier.get();
        } finally {
            redisHelper.clearCurrentDatabase();
        }
    }

    /**
     * 带返回结果的redis操作
     *
     * @param db       redis db
     * @param function 函数式接口
     * @return T t
     * @author Mr_wenpan@163.com 2021/8/12 3:09 下午
     */
    public static <T> T executeWithResult(int db, Function<RedisTemplate<String, String>, T> function) {
        T result;
        setCurrentDatabase(db);
        try {
            result = function.apply(redisHelper.getRedisTemplate());
        } finally {
            clearCurrentDatabase();
        }
        return result;
    }

    /**
     * 有返回值 Redis 操作
     *
     * @param db       Redis db
     * @param executor Redis 操作
     * @param <T>      返回类型
     * @return Redis 操作返回值
     * @since 1.7
     */
    public static <T> T execute(int db, WithResultExecutor<T> executor) {
        try {
            redisHelper.setCurrentDatabase(db);
            return executor.get(redisHelper);
        } finally {
            redisHelper.clearCurrentDatabase();
        }
    }

    @FunctionalInterface
    public interface NoneResultExecutor {
        void accept(RedisHelper helper);
    }

    @FunctionalInterface
    public interface WithResultExecutor<T> {
        T get(RedisHelper helper);
    }


}
