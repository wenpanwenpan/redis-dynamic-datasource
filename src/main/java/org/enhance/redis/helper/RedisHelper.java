package org.enhance.redis.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;
import org.enhance.redis.convert.DateDeserializer;
import org.enhance.redis.convert.DateSerializer;
import org.enhance.redis.options.AbstractOptionsRedisDb;
import org.enhance.redis.options.DefaultOptionsRedisDb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis操作工具类 集成封装一些常用方法
 *
 * @author Mr_wenpan@163.com 2021/8/7 2:52 下午
 */
@Component
public class RedisHelper implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(RedisHelper.class);

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /**
     * 默认过期时长，单位：秒
     */
    public static final long DEFAULT_EXPIRE = 60 * 60 * 24L;

    /**
     * 不设置过期时长
     */
    public static final long NOT_EXPIRE = -1;

    /**
     * objectMapper对象
     */
    static final ObjectMapper objectMapper;

    /**
     * 使用连向默认的redis实例的redisTemplate
     */
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 这里按RedisTemplate的设计模式设计，每一个RedisHelper一个AbstractOptionsRedisDb对象
     */
    private AbstractOptionsRedisDb<String, String> optionsRedisDb = new DefaultOptionsRedisDb(this);

    static {
        objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(Date.class, new DateSerializer());
        javaTimeModule.addDeserializer(Date.class, new DateDeserializer());
        objectMapper.registerModule(javaTimeModule);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public RedisHelper(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(redisTemplate, "redisTemplate must not be null.");
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * 获取RedisTemplate对象
     *
     * @return RedisTemplate
     */
    public RedisTemplate<String, String> getRedisTemplate() {
        return redisTemplate;
    }

    protected ValueOperations<String, String> getValueOperations() {
        return redisTemplate.opsForValue();
    }

    protected HashOperations<String, String, String> getHashOperations() {
        return redisTemplate.opsForHash();
    }

    protected ListOperations<String, String> getListOperations() {
        return redisTemplate.opsForList();
    }

    protected SetOperations<String, String> getSetOperations() {
        return redisTemplate.opsForSet();
    }

    protected ZSetOperations<String, String> getZSetOperations() {
        return redisTemplate.opsForZSet();
    }

    /**
     * 设置当前线程操作 redis database，同一个线程内操作多次redis，不同database，
     * 需要调用 {@link RedisHelper#clearCurrentDatabase()} 清除当前线程redis database，从而使用默认的db.
     * 如果静态RedisHelper进行db切换，这是不被允许的，需要抛出异常
     *
     * @param database redis database
     */
    public void setCurrentDatabase(int database) {
        logger.warn("Use default RedisHelper, you'd better use a DynamicRedisHelper instead.");
        throw new RuntimeException("static redisHelper can't change db.");
    }

    /**
     * 清除当前线程 redis database.
     */
    public void clearCurrentDatabase() {
        logger.warn("Use default RedisHelper, you'd better use a DynamicRedisHelper instead.");
    }

    /**
     * 获取RedisTemplates，静态RedisHelper中没有多个RedisTemplates，交给子类实现(这里适配其他功能)
     */
    public Map<Object, RedisTemplate<String, String>> getRedisTemplates() {
        return null;
    }

    /**
     * 操作redis db，获取操作对象
     */
    public AbstractOptionsRedisDb<String, String> opsDb() {
        return optionsRedisDb;
    }

    /**
     * 操作1号db
     */
    public RedisTemplate<String, String> opsDbZero() {
        return optionsRedisDb.opsDbZero();
    }

    public RedisTemplate<String, String> opsDbOne() {
        return optionsRedisDb.opsDbOne();
    }

    public RedisTemplate<String, String> opsDbTwo() {
        return optionsRedisDb.opsDbTwo();
    }

    public RedisTemplate<String, String> opsDbThree() {
        return optionsRedisDb.opsDbThree();
    }

    public RedisTemplate<String, String> opsDbFour() {
        return optionsRedisDb.opsDbFour();
    }

    public RedisTemplate<String, String> opsDbFive() {
        return optionsRedisDb.opsDbFive();
    }

    public RedisTemplate<String, String> opsDbSix() {
        return optionsRedisDb.opsDbSix();
    }

    public RedisTemplate<String, String> opsDbSeven() {
        return optionsRedisDb.opsDbSeven();
    }

    public RedisTemplate<String, String> opsDbEight() {
        return optionsRedisDb.opsDbEight();
    }

    public RedisTemplate<String, String> opsDbNine() {
        return optionsRedisDb.opsDbNine();
    }

    public RedisTemplate<String, String> opsDbTen() {
        return optionsRedisDb.opsDbTen();
    }

    public RedisTemplate<String, String> opsDbEleven() {
        return optionsRedisDb.opsDbEleven();
    }

    public RedisTemplate<String, String> opsDbTwelve() {
        return optionsRedisDb.opsDbTwelve();
    }

    public RedisTemplate<String, String> opsDbThirteen() {
        return optionsRedisDb.opsDbThirteen();
    }

    public RedisTemplate<String, String> opsDbFourteen() {
        return optionsRedisDb.opsDbFourteen();
    }

    public RedisTemplate<String, String> opsDbFifteen() {
        return optionsRedisDb.opsDbFifteen();
    }

    public RedisTemplate<String, String> opsOtherDb(int db) {

        return optionsRedisDb.opsOtherDb(db);
    }

    /**
     * 将 Lua 脚本封装到 RedisScript 中执行
     *
     * @param redisScript Lua 脚本
     * @param keys        脚本中对应的key，可以用 KEYS[1]、KEYS[2]... 获取
     * @param args        脚本中用到的参数，可以用 ARGV[1]、ARGV[2]... 获取
     * @param <T>         返回类型
     * @return T
     */
    public <T> T executeScript(RedisScript<T> redisScript, List<String> keys, List<Object> args) {
        return getRedisTemplate().execute(redisScript, keys, args.toArray());
    }

    // =======================================以下是基于redisTemplate封装的redis操作===================================

    /**
     * 删除key
     *
     * @param key key
     */
    public void delKey(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 删除key
     *
     * @param key key
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * returns -2 if the key does not exist.
     * returns -1 if the key exists but has no associated expire.
     *
     * @param key key
     * @return TTL in seconds, or a negative value in order to signal an error
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * returns -2 if the key does not exist.
     * returns -1 if the key exists but has no associated expire.
     *
     * @param key      key
     * @param timeUnit timeUnit
     * @return TTL in seconds, or a negative value in order to signal an error
     */
    public Long getExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }

    /**
     * 设置过期时间,默认一天
     *
     * @param key key
     */
    public Boolean setExpire(String key) {
        return setExpire(key, DEFAULT_EXPIRE, TimeUnit.SECONDS);
    }

    /**
     * 设置过期时间,默认时间单位:秒
     *
     * @param key    key
     * @param expire 存活时长
     */
    public Boolean setExpire(String key, long expire) {
        return setExpire(key, expire, TimeUnit.SECONDS);
    }

    /**
     * 设置过期时间
     *
     * @param key      key
     * @param expire   存活时长
     * @param timeUnit 时间单位
     */
    public Boolean setExpire(String key, long expire, TimeUnit timeUnit) {
        return redisTemplate.expire(key, expire, timeUnit == null ? TimeUnit.SECONDS : timeUnit);
    }

    /**
     * 批量删除Key
     *
     * @param keys 待删除的keys
     */
    public void delKeys(Collection<String> keys) {
        redisTemplate.delete(new HashSet<>(keys));
    }

    /**
     * 删除一个完整的key
     *
     * @param fullKey 完整的key
     */
    private void deleteFullKey(String fullKey) {
        redisTemplate.delete(fullKey);
    }

    /**
     * 批量删除完整的key
     *
     * @param fullKeys 完整的key集合
     */
    private void deleteFullKeys(Collection<String> fullKeys) {
        redisTemplate.delete(fullKeys);
    }

    /**
     * String 设置值
     *
     * @param key    key
     * @param value  value
     * @param expire 过期时间
     */
    public void strSet(String key, String value, long expire, TimeUnit timeUnit) {
        getValueOperations().set(key, value);
        if (expire != NOT_EXPIRE) {
            setExpire(key, expire, timeUnit == null ? TimeUnit.SECONDS : timeUnit);
        }
    }

    /**
     * String 设置值
     *
     * @param key   key
     * @param value value
     */
    public void strSet(String key, String value) {
        getValueOperations().set(key, value);
    }

    /**
     * String 获取值
     *
     * @param key key
     */
    public String strGet(String key) {
        return getValueOperations().get(key);
    }

    /**
     * String 获取值
     *
     * @param key    key
     * @param expire 过期时间
     */
    public String strGet(String key, long expire, TimeUnit timeUnit) {
        String value = getValueOperations().get(key);
        if (expire != NOT_EXPIRE) {
            setExpire(key, expire, timeUnit == null ? TimeUnit.SECONDS : timeUnit);
        }
        return value;
    }

    /**
     * String 获取值
     *
     * @param key   key
     * @param clazz 待转换的类Class
     */
    public <T> T strGet(String key, Class<T> clazz) {
        String value = getValueOperations().get(key);
        return value == null ? null : fromJson(value, clazz);
    }

    /**
     * String 设置值
     *
     * @param key    key
     * @param clazz  clazz
     * @param expire 过期时间
     */
    public <T> T strGet(String key, Class<T> clazz, long expire, TimeUnit timeUnit) {
        String value = getValueOperations().get(key);
        if (expire != NOT_EXPIRE) {
            setExpire(key, expire, timeUnit == null ? TimeUnit.SECONDS : timeUnit);
        }
        return value == null ? null : fromJson(value, clazz);
    }

    /**
     * String 获取值
     *
     * @param key   key
     * @param start 开始的位置
     * @param end   结束的位置
     */
    public String strGet(String key, Long start, Long end) {
        return getValueOperations().get(key, start, end);
    }

    /**
     * 如果值不存在则设置（原子操作）
     *
     * @param key   key
     * @param value value
     */
    public Boolean strSetIfAbsent(String key, String value) {
        return getValueOperations().setIfAbsent(key, value);
    }

    /**
     * String 获取自增字段，递减字段可使用delta为负数的方式
     *
     * @param key   key
     * @param delta delta
     */
    public Long strIncrement(String key, Long delta) {
        return getValueOperations().increment(key, delta);
    }

    /**
     * List 推入数据至列表左端
     *
     * @param key   key
     * @param value value
     */
    public Long lstLeftPush(String key, String value) {
        return getListOperations().leftPush(key, value);
    }

    /**
     * List 推入数据至列表左端
     *
     * @param key    key
     * @param values Collection集合
     */
    public Long lstLeftPushAll(String key, Collection<String> values) {
        return getListOperations().leftPushAll(key, values);
    }

    /**
     * List 推入数据至列表右端
     *
     * @param key   key
     * @param value value
     */
    public Long lstRightPush(String key, String value) {
        return getListOperations().rightPush(key, value);
    }

    /**
     * List 推入数据至列表右端
     *
     * @param key    key
     * @param values Collection集合
     */
    public Long lstRightPushAll(String key, Collection<String> values) {
        return getListOperations().rightPushAll(key, values);
    }

    /**
     * List 返回列表键key中，从索引start至索引end范围的所有列表项。两个索引都可以是正数或负数
     *
     * @param key   key
     * @param start 开始位置
     * @param end   结束位置
     */
    public List<String> lstRange(String key, long start, long end) {
        return getListOperations().range(key, start, end);
    }

    /**
     * List 返回列表键key中所有的元素
     *
     * @param key key
     */
    public List<String> lstAll(String key) {
        return lstRange(key, 0, lstLen(key));
    }

    /**
     * List 移除并返回列表最左端的项
     *
     * @param key key
     */
    public String lstLeftPop(String key) {
        return getListOperations().leftPop(key);
    }

    /**
     * List 移除并返回列表最右端的项
     *
     * @param key key
     */
    public String lstRightPop(String key) {
        return getListOperations().rightPop(key);
    }

    /**
     * List 移除并返回列表最左端的项
     *
     * @param key     key
     * @param timeout 等待超时时间
     */
    public String lstLeftPop(String key, long timeout, TimeUnit timeUnit) {
        return getListOperations().leftPop(key, timeout, timeUnit);
    }

    /**
     * List 移除并返回列表最右端的项
     *
     * @param key     key
     * @param timeout 等待超时时间
     */
    public String lstRightPop(String key, long timeout, TimeUnit timeUnit) {
        return getListOperations().rightPop(key, timeout, timeUnit);
    }

    /**
     * List 返回指定key的长度
     *
     * @param key key
     */
    public Long lstLen(String key) {
        return getListOperations().size(key);
    }

    /**
     * List 设置指定索引上的列表项。将列表键 key索引index上的列表项设置为value。 如果index参数超过了列表的索引范围，那么命令返回了一个错误
     *
     * @param key   key
     * @param index index
     * @param value value
     */
    public void lstSet(String key, long index, String value) {
        getListOperations().set(key, index, value);
    }

    /**
     * List 根据参数 count的值，移除列表中与参数value相等的元素。 count的值可以是以下几种：count &gt; 0 :从表头开始向表尾搜索，移除与 value相等的元素，数量为
     * count
     *
     * @param key   key
     * @param index index
     * @param value value
     */
    public Long lstRemove(String key, long index, String value) {
        return getListOperations().remove(key, index, value);
    }

    /**
     * List 返回列表键key中，指定索引index上的列表项。index索引可以是正数或者负数
     *
     * @param key   key
     * @param index index
     */
    public Object lstIndex(String key, long index) {
        return getListOperations().index(key, index);
    }

    /**
     * List 对一个列表进行修剪(trim)，让列表只保留指定索引范围内的列表项，而将不在范围内的其它列表项全部删除。 两个索引都可以是正数或者负数
     *
     * @param key   key
     * @param start 开始位置
     * @param end   结束位置
     */
    public void lstTrim(String key, long start, long end) {
        getListOperations().trim(key, start, end);
    }

    /**
     * Set 将数组添加到给定的集合里面，已经存在于集合的元素会自动的被忽略， 命令返回新添加到集合的元素数量。
     *
     * @param key    key
     * @param values values
     */
    public Long setAdd(String key, String[] values) {
        return getSetOperations().add(key, values);
    }

    /**
     * Set 将一个或多个元素添加到给定的集合里面，已经存在于集合的元素会自动的被忽略， 命令返回新添加到集合的元素数量。
     *
     * @param key    key
     * @param values values
     */
    public Long setIrt(String key, String... values) {
        return getSetOperations().add(key, values);
    }

    /**
     * Set 将返回集合中所有的元素。
     *
     * @param key key
     */
    public Set<String> setMembers(String key) {
        return getSetOperations().members(key);
    }

    /**
     * Set 检查给定的元素是否存在于集合
     *
     * @param key key
     */
    public Boolean setIsmember(String key, String o) {
        return getSetOperations().isMember(key, o);
    }

    /**
     * Set 返回集合包含的元素数量（也即是集合的基数）
     *
     * @param key key
     */
    public Long setSize(String key) {
        return getSetOperations().size(key);
    }

    /**
     * Set 计算所有给定集合的交集，并返回结果
     *
     * @param key      key
     * @param otherKey otherKey
     */
    public Set<String> setIntersect(String key, String otherKey) {
        return getSetOperations().intersect(key, otherKey);
    }

    /**
     * Set 计算所有的并集并返回结果
     *
     * @param key      key
     * @param otherKey otherKey
     */
    public Set<String> setUnion(String key, String otherKey) {
        return getSetOperations().union(key, otherKey);
    }

    /**
     * Set 计算所有的并集并返回结果
     *
     * @param key       key
     * @param otherKeys otherKey
     */
    public Set<String> setUnion(String key, Collection<String> otherKeys) {
        return getSetOperations().union(key, otherKeys);
    }

    /**
     * Set 返回一个集合的全部成员，该集合是所有给定集合之间的差集
     *
     * @param key      key
     * @param otherKey otherKey
     */
    public Set<String> setDifference(String key, String otherKey) {
        return getSetOperations().difference(key, otherKey);
    }

    /**
     * Set 返回一个集合的全部成员，该集合是所有给定集合之间的差集
     *
     * @param key       key
     * @param otherKeys otherKeys
     */
    public Set<String> setDifference(String key, Collection<String> otherKeys) {
        return getSetOperations().difference(key, otherKeys);
    }

    /**
     * set 删除数据
     *
     * @param key   key
     * @param value value
     */
    public Long setDel(String key, String value) {
        return getSetOperations().remove(key, value);
    }

    /**
     * Set 批量删除数据
     *
     * @param key   key
     * @param value value
     */
    public Long setRemove(String key, Object[] value) {
        return getSetOperations().remove(key, value);
    }

    /**
     * ZSet Zadd 命令用于将一个或多个成员元素及其分数值加入到有序集当中。 如果某个成员已经是有序集的成员，那么更新这个成员的分数值，并通过重新插入这个成员元素，来保证该成员在正确的位置上。
     * 分数值可以是整数值或双精度浮点数。 如果有序集合 key 不存在，则创建一个空的有序集并执行 ZADD 操作。 当 key 存在但不是有序集类型时，返回一个错误。
     *
     * @param key   key
     * @param value 值
     * @param score 得分
     */
    public Boolean zSetAdd(String key, String value, double score) {
        return getZSetOperations().add(key, value, score);
    }

    /**
     * ZSet 返回有序集合中，指定元素的分值
     */
    public Double zSetScore(String key, String value) {
        return getZSetOperations().score(key, value);
    }

    /**
     * ZSet 为有序集合指定元素的分值加上增量increment，命令返回执行操作之后，元素的分值 可以通过将 increment设置为负数来减少分值
     */
    public Double zSetIncrementScore(String key, String value, double delta) {
        return getZSetOperations().incrementScore(key, value, delta);
    }

    /**
     * ZSet 返回指定元素在有序集合中的排名，其中排名按照元素的分值从小到大计算。排名以 0 开始
     */
    public Long zSetRank(String key, String value) {
        return getZSetOperations().rank(key, value);
    }

    /**
     * ZSet 返回成员在有序集合中的逆序排名，其中排名按照元素的分值从大到小计算
     */
    public Long zSetReverseRank(String key, String value) {
        return getZSetOperations().reverseRank(key, value);
    }

    /**
     * ZSet 返回有序集合的基数
     */
    public Long zSetSize(String key) {
        return getZSetOperations().size(key);
    }

    /**
     * ZSet 删除数据
     */
    public Long zSetRemove(String key, String value) {
        return getZSetOperations().remove(key, value);
    }

    /**
     * ZSet 根据score区间删除数据
     */
    public Long zSetRemoveByScore(String key, double min, double max) {
        return getZSetOperations().removeRangeByScore(key, min, max);
    }


    /**
     * ZSet 返回有序集中指定分数区间内的所有的成员。有序集成员按分数值递减(从大到小)的次序排列。 具有相同分数值的成员按字典序的逆序(reverse lexicographical order
     * )排列。
     */
    public Set<String> zSetRange(String key, Long start, Long end) {
        return getZSetOperations().range(key, start, end);
    }

    /**
     * ZSet
     */
    public Set<String> zSetReverseRange(String key, Long start, Long end) {
        return getZSetOperations().reverseRange(key, start, end);
    }

    /**
     * ZSet 返回有序集合在按照分值升序排列元素的情况下，分值在 min 和 max范围之内的所有元素
     */
    public Set<String> zSetRangeByScore(String key, Double min, Double max) {
        return getZSetOperations().rangeByScore(key, min, max);
    }

    /**
     * ZSet 返回有序集合在按照分值降序排列元素的情况下，分值在 min 和 max范围之内的所有元素
     */
    public Set<String> zSetReverseRangeByScore(String key, Double min, Double max) {
        return getZSetOperations().reverseRangeByScore(key, min, max);
    }

    /**
     * ZSet 返回有序集中指定分数区间内的所有的成员。有序集成员按分数值递减(从小到大)的次序排列。 具有相同分数值的成员按字典序的顺序(reverse lexicographical order
     * )排列。
     */
    public Set<String> zSetRangeByScore(String key, Double min, Double max, Long offset, Long count) {
        return getZSetOperations().rangeByScore(key, min, max, offset, count);
    }

    /**
     * 返回有序集中指定分数区间内的所有的成员。有序集成员按分数值递减(从大到小)的次序排列。 具有相同分数值的成员按字典序的逆序(reverse lexicographical order )排列。
     */
    public Set<String> zSetReverseRangeByScore(String key, Double min, Double max, Long offset, Long count) {
        return getZSetOperations().reverseRangeByScore(key, min, max, offset, count);
    }

    /**
     * ZSet 返回有序集合在升序排列元素的情况下，分值在 min和 max范围内的元素数量
     */
    public Long zSetCount(String key, Double min, Double max) {
        return getZSetOperations().count(key, min, max);
    }

    /**
     * Hash 将哈希表 key 中的域 field的值设为 value。如果 key不存在，一个新的哈希表被创建并进行HSET操作。 如果域 field已经存在于哈希表中，旧值将被覆盖
     *
     * @param key     key
     * @param hashKey hashKey
     * @param value   value
     */
    public void hshPut(String key, String hashKey, String value) {
        getHashOperations().put(key, hashKey, value);
    }

    /**
     * Hash 批量插入值，Map的key代表Field
     *
     * @param key key
     * @param map map
     */
    public void hshPutAll(String key, Map<String, String> map) {
        getHashOperations().putAll(key, map);
    }

    /**
     * 获取hash对象中的对象序列字符
     *
     * @param key     key
     * @param hashKey hashKey
     */
    public byte[] hshGetSerial(String key, String hashKey) {
        RedisSerializer<String> redisSerializer = redisTemplate.getStringSerializer();
        return redisTemplate.execute((RedisCallback<byte[]>) connection -> {
            try {
                return connection.hGet(Objects.requireNonNull(redisSerializer.serialize(key)),
                        Objects.requireNonNull(redisSerializer.serialize(hashKey)));
            } catch (Exception e) {
                logger.error("获取HASH对象序列失败", e);
            }
            return null;
        });
    }

    /**
     * 插入hash对象序列值
     *
     * @param key     key
     * @param hashKey hashKey
     * @param value   value
     */
    public Boolean hshPutSerial(String key, String hashKey, byte[] value) {
        RedisSerializer<String> redisSerializer = redisTemplate.getStringSerializer();
        return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            try {
                return connection.hSet(Objects.requireNonNull(redisSerializer.serialize(key)),
                        Objects.requireNonNull(redisSerializer.serialize(hashKey)),
                        value);
            } catch (Exception e) {
                logger.error("插入HASH对象序列失败", e);
            }
            return Boolean.FALSE;
        });
    }

    /**
     * Hash 返回哈希表 key 中给定域 field的值，返回值：给定域的值。当给定域不存在或是给定 key不存在时，返回 nil。
     *
     * @param key     key
     * @param hashKey hashKey
     */
    public String hshGet(String key, String hashKey) {
        return getHashOperations().get(key, hashKey);
    }

    /**
     * Hash 返回散列键 key 中，一个或多个域的值，相当于同时执行多个 HGET
     *
     * @param key      key
     * @param hashKeys hashKeys
     */
    public List<String> hshMultiGet(String key, Collection<String> hashKeys) {
        return getHashOperations().multiGet(key, hashKeys);
    }

    /**
     * Hash 获取散列Key中所有的键值对
     *
     * @param key key
     */
    public Map<String, String> hshGetAll(String key) {
        return getHashOperations().entries(key);
    }

    /**
     * Hash 查看哈希表 key 中，给定域 field是否存在
     *
     * @param key     key
     * @param hashKey hashKey
     */
    public Boolean hshHasKey(String key, String hashKey) {
        return getHashOperations().hasKey(key, hashKey);
    }

    /**
     * Hash 返回哈希表 key 中的所有域
     *
     * @param key key
     */
    public Set<String> hshKeys(String key) {
        return getHashOperations().keys(key);
    }

    /**
     * Hash 返回散列键 key 中，所有域的值
     *
     * @param key key
     */
    public List<String> hshVals(String key) {
        return getHashOperations().values(key);
    }

    /**
     * Hash 返回散列键 key中指定Field的域的值
     *
     * @param key      key
     * @param hashKeys hashKeys
     */
    public List<String> hshVals(String key, Collection<String> hashKeys) {
        return getHashOperations().multiGet(key, hashKeys);
    }

    /**
     * Hash 散列键 key的数量
     *
     * @param key key
     */
    public Long hshSize(String key) {
        return getHashOperations().size(key);
    }

    /**
     * Hash 删除散列键 key 中的一个或多个指定域，以及那些域的值。不存在的域将被忽略。命令返回被成功删除的域值对数量
     *
     * @param key      key
     * @param hashKeys hashKeys
     */
    public void hshDelete(String key, Object... hashKeys) {
        getHashOperations().delete(key, hashKeys);
    }

    /**
     * Hash 删除散列键 key的数组
     *
     * @param key      key
     * @param hashKeys hashKeys
     */
    public void hshRemove(String key, Object[] hashKeys) {
        getHashOperations().delete(key, hashKeys);
    }

    /**
     * Object转成JSON数据
     */
    public static <T> String toJson(T object) {
        if (object == null) {
            return StringUtils.EMPTY;
        }
        if (object instanceof Integer || object instanceof Long || object instanceof Float || object instanceof Double
                || object instanceof Boolean || object instanceof String) {
            return String.valueOf(object);
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * JSON数据，转成Object
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json) || clazz == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * JSON数据，转成Object
     */
    public static <T> T fromJson(String json, TypeReference<T> valueTypeRef) {
        if (StringUtils.isBlank(json) || valueTypeRef == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, valueTypeRef);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getMessage(), e);
            }
            return null;
        }
    }

    /**
     * JSON数据，转成 List&lt;Object&gt;
     */
    public static <T> List<T> fromJsonList(String json, Class<T> clazz) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(List.class, clazz);
        try {
            return objectMapper.readValue(json, javaType);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    /**
     * 将对象直接以json数据不设置过期时间的方式保存
     *
     * @param key    键
     * @param object object
     */
    public <T> void objectSet(String key, T object) {
        strSet(key, toJson(object));
    }

    /**
     * 根据一个前缀来删除所有匹配的key
     *
     * @param keyPrefix 前缀
     * @return 删除的数量
     * @deprecated 集群环境不可用
     */
    @Deprecated
    public int deleteKeysWithPrefix(String keyPrefix) {
        Set<String> keys = keys(keyPrefix + '*');
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        deleteFullKeys(keys);
        return keys.size();
    }

    /**
     * 根据表达式获取匹配的所有key
     *
     * @param pattern 表达式
     * @return 匹配的所有key
     * @deprecated 集群环境不可用
     */
    @Deprecated
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

}
