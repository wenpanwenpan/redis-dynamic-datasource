package org.enhance.redis.client;

import org.enhance.redis.helper.RedisHelper;
import org.enhance.redis.infra.constant.DynamicRedisConstants;
import org.enhance.redis.options.AbstractOptionsRedisDb;
import org.enhance.redis.register.RedisDataSourceRegister;
import org.springframework.data.redis.core.RedisTemplate;

import static org.enhance.redis.infra.constant.DynamicRedisConstants.MultiSource.DEFAULT_SOURCE;

/**
 * Redis多数据源操作客户端
 *
 * @author Mr_wenpan@163.com 2021/09/06 11:10
 */
public class RedisMultiSourceClient {

    /**
     * 操作指定数据源的默认db
     *
     * @param datasource 数据源名称
     */
    public RedisTemplate<String, String> opsDefaultDb(String datasource) {
        // 获取该数据源的默认db对应的redisTemplate
        RedisTemplate<String, String> redisTemplate = RedisDataSourceRegister.getRedisTemplate(
                datasource + DynamicRedisConstants.MultiSource.REDIS_TEMPLATE);
        if (null == redisTemplate) {
            throw new RuntimeException("没有该数据源，请确认传入的redis数据源名称是否正确.");
        }
        return redisTemplate;
    }

    public RedisTemplate<String, String> opsDbZeroWithDefaultSource() {

        return commonOpsDb(DEFAULT_SOURCE).opsDbZero();
    }

    public RedisTemplate<String, String> opsDbOneWithDefaultSource() {

        return commonOpsDb(DEFAULT_SOURCE).opsDbOne();
    }

    public RedisTemplate<String, String> opsDbTwoWithDefaultSource() {

        return commonOpsDb(DEFAULT_SOURCE).opsDbTwo();
    }

    public RedisTemplate<String, String> opsDbThreeWithDefaultSource() {

        return commonOpsDb(DEFAULT_SOURCE).opsDbThree();
    }

    public RedisTemplate<String, String> opsDbFourWithDefaultSource() {

        return commonOpsDb(DEFAULT_SOURCE).opsDbFour();
    }

    public RedisTemplate<String, String> opsDbFiveWithDefaultSource() {

        return commonOpsDb(DEFAULT_SOURCE).opsDbFive();
    }

    public RedisTemplate<String, String> opsDbSixWithDefaultSource() {

        return commonOpsDb(DEFAULT_SOURCE).opsDbSix();
    }

    public RedisTemplate<String, String> opsDbSevenWithDefaultSource() {

        return commonOpsDb(DEFAULT_SOURCE).opsDbSeven();
    }

    public RedisTemplate<String, String> opsDbEightWithDefaultSource() {

        return commonOpsDb(DEFAULT_SOURCE).opsDbEight();
    }

    public RedisTemplate<String, String> opsDbNineWithDefaultSource() {

        return commonOpsDb(DEFAULT_SOURCE).opsDbNine();
    }

    public RedisTemplate<String, String> opsDbTenWithDefaultSource() {

        return commonOpsDb(DEFAULT_SOURCE).opsDbTen();
    }

    public RedisTemplate<String, String> opsDbElevenWithDefaultSource() {

        return commonOpsDb(DEFAULT_SOURCE).opsDbEleven();
    }

    public RedisTemplate<String, String> opsDbTwelveWithDefaultSource() {

        return commonOpsDb(DEFAULT_SOURCE).opsDbTwelve();
    }

    public RedisTemplate<String, String> opsDbThirteenWithDefaultSource() {

        return commonOpsDb(DEFAULT_SOURCE).opsDbThirteen();
    }

    public RedisTemplate<String, String> opsDbFourteenWithDefaultSource() {

        return commonOpsDb(DEFAULT_SOURCE).opsDbFourteen();
    }

    public RedisTemplate<String, String> opsDbFifteenWithDefaultSource() {
        return commonOpsDb(DEFAULT_SOURCE).opsDbFifteen();
    }

    /**
     * 使用默认数据源，操作0~15号库以外的db
     */
    public RedisTemplate<String, String> opsOtherDbWithDefaultSource(int db) {

        return commonOpsDb(DEFAULT_SOURCE).opsOtherDb(db);
    }

    public RedisTemplate<String, String> opsDbZero(String datasource) {

        return commonOpsDb(datasource).opsDbZero();
    }

    public RedisTemplate<String, String> opsDbOne(String datasource) {

        return commonOpsDb(datasource).opsDbOne();
    }

    public RedisTemplate<String, String> opsDbTwo(String datasource) {

        return commonOpsDb(datasource).opsDbTwo();
    }

    public RedisTemplate<String, String> opsDbThree(String datasource) {

        return commonOpsDb(datasource).opsDbThree();
    }

    public RedisTemplate<String, String> opsDbFour(String datasource) {

        return commonOpsDb(datasource).opsDbFour();
    }

    public RedisTemplate<String, String> opsDbFive(String datasource) {

        return commonOpsDb(datasource).opsDbFive();
    }

    public RedisTemplate<String, String> opsDbSix(String datasource) {

        return commonOpsDb(datasource).opsDbSix();
    }

    public RedisTemplate<String, String> opsDbSeven(String datasource) {

        return commonOpsDb(datasource).opsDbSeven();
    }

    public RedisTemplate<String, String> opsDbEight(String datasource) {

        return commonOpsDb(datasource).opsDbEight();
    }

    public RedisTemplate<String, String> opsDbNine(String datasource) {

        return commonOpsDb(datasource).opsDbNine();
    }

    public RedisTemplate<String, String> opsDbTen(String datasource) {

        return commonOpsDb(datasource).opsDbTen();
    }

    public RedisTemplate<String, String> opsDbEleven(String datasource) {

        return commonOpsDb(datasource).opsDbEleven();
    }

    public RedisTemplate<String, String> opsDbTwelve(String datasource) {

        return commonOpsDb(datasource).opsDbTwelve();
    }

    public RedisTemplate<String, String> opsDbThirteen(String datasource) {

        return commonOpsDb(datasource).opsDbThirteen();
    }

    public RedisTemplate<String, String> opsDbFourteen(String datasource) {

        return commonOpsDb(datasource).opsDbFourteen();
    }

    public RedisTemplate<String, String> opsDbFifteen(String datasource) {
        return commonOpsDb(datasource).opsDbFifteen();
    }

    /**
     * 操作0~15号库以外的db
     */
    public RedisTemplate<String, String> opsOtherDb(String datasource, int db) {

        return commonOpsDb(datasource).opsOtherDb(db);
    }

    private AbstractOptionsRedisDb<String, String> commonOpsDb(String datasource) {
        // 获取该数据源对应的redisHelper
        RedisHelper redisHelper = RedisDataSourceRegister.getRedisHelper(datasource + DynamicRedisConstants.MultiSource.REDIS_HELPER);
        if (redisHelper == null) {
            throw new RuntimeException("没有该数据源，请确认传入的redis数据源名称是否正确.");
        }
        // 获取指定db的redisTemplate
        return redisHelper.opsDb();
    }

}
