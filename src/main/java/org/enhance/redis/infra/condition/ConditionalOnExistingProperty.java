package org.enhance.redis.infra.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * 存在这个属性时才满足
 *
 * @author Mr_wenpan@163.com 2021/8/22 10:40 下午
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(OnExistingPropertyCondition.class)
public @interface ConditionalOnExistingProperty {

    /**
     * 属性值，多个值要求同时为空
     *
     * @return 属性配置
     */
    String[] value() default {};

}