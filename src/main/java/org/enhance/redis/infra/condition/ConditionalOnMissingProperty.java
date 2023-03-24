package org.enhance.redis.infra.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * 不存在这个属性时才满足
 *
 * @author Mr_wenpan@163.com 2021/8/22 10:04 下午
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Conditional(OnMissingPropertyCondition.class)
public @interface ConditionalOnMissingProperty {

    /**
     * 属性值，多个值要求同时为空
     *
     * @return 属性配置
     */
    String[] value() default {};
}