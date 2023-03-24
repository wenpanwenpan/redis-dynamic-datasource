package org.enhance.redis.infra.function;

/**
 * 不需要参数不需要返回值的函数式接口
 *
 * @author Mr_wenpan@163.com 2021/08/11 21:22
 */
@FunctionalInterface
public interface Execute {

    /**
     * 执行任务
     */
    void execute();
}
