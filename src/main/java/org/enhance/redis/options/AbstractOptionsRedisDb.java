package org.enhance.redis.options;

import org.enhance.redis.helper.RedisHelper;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 操作redis db
 *
 * @author Mr_wenpan@163.com 2021/09/06 13:57
 */
public abstract class AbstractOptionsRedisDb<T, K> {

    RedisHelper redisHelper;

    public AbstractOptionsRedisDb(RedisHelper redisHelper) {
        this.redisHelper = redisHelper;
    }

    /**
     * 操作0号db
     *
     * @return org.springframework.data.redis.core.RedisTemplate<T, K>
     */
    public abstract RedisTemplate<T, K> opsDbZero();


    /**
     * 操作一号db
     *
     * @return org.springframework.data.redis.core.RedisTemplate<T, K>
     */
    public abstract RedisTemplate<T, K> opsDbOne();

    /**
     * 操作二号db
     *
     * @return org.springframework.data.redis.core.RedisTemplate<T, K>
     */
    public abstract RedisTemplate<T, K> opsDbTwo();

    /**
     * 操作三号db
     *
     * @return org.springframework.data.redis.core.RedisTemplate<T, K>
     */
    public abstract RedisTemplate<T, K> opsDbThree();

    /**
     * 操作四号db
     *
     * @return org.springframework.data.redis.core.RedisTemplate<T, K>
     */
    public abstract RedisTemplate<T, K> opsDbFour();

    /**
     * 操作五号db
     *
     * @return org.springframework.data.redis.core.RedisTemplate<T, K>
     */
    public abstract RedisTemplate<T, K> opsDbFive();

    /**
     * 操作六号db
     *
     * @return org.springframework.data.redis.core.RedisTemplate<T, K>
     */
    public abstract RedisTemplate<T, K> opsDbSix();

    /**
     * 操作七号db
     *
     * @return org.springframework.data.redis.core.RedisTemplate<T, K>
     */
    public abstract RedisTemplate<T, K> opsDbSeven();

    /**
     * 操作八号db
     *
     * @return org.springframework.data.redis.core.RedisTemplate<T, K>
     */
    public abstract RedisTemplate<T, K> opsDbEight();

    /**
     * 操作九号db
     *
     * @return org.springframework.data.redis.core.RedisTemplate<T, K>
     */
    public abstract RedisTemplate<T, K> opsDbNine();

    /**
     * 操作十号db
     *
     * @return org.springframework.data.redis.core.RedisTemplate<T, K>
     */
    public abstract RedisTemplate<T, K> opsDbTen();

    /**
     * 操作十一号db
     *
     * @return org.springframework.data.redis.core.RedisTemplate<T, K>
     */
    public abstract RedisTemplate<T, K> opsDbEleven();

    /**
     * 操作十二号db
     *
     * @return org.springframework.data.redis.core.RedisTemplate<T, K>
     */
    public abstract RedisTemplate<T, K> opsDbTwelve();

    /**
     * 操作十三号db
     *
     * @return org.springframework.data.redis.core.RedisTemplate<T, K>
     */
    public abstract RedisTemplate<T, K> opsDbThirteen();

    /**
     * 操作十四号db
     *
     * @return org.springframework.data.redis.core.RedisTemplate<T, K>
     */
    public abstract RedisTemplate<T, K> opsDbFourteen();

    /**
     * 操作十五号db
     *
     * @return org.springframework.data.redis.core.RedisTemplate<T, K>
     */
    public abstract RedisTemplate<T, K> opsDbFifteen();

    /**
     * 操作其他redis db(默认提供操作16个db的通用方法，和redis服务对应。如果还想操作除了这16个db之外的其他db，则可以使用这个方法)
     *
     * @param db db号
     * @return org.springframework.data.redis.core.RedisTemplate<T, K>
     */
    public abstract RedisTemplate<T, K> opsOtherDb(int db);

}
