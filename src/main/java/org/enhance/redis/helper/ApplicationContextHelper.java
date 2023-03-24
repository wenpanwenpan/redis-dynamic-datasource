package org.enhance.redis.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * spring工厂调用辅助类
 *
 * @author Mr_wenpan@163.com 2021/8/11 8:19 下午
 */
public class ApplicationContextHelper implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationContextHelper.class);

    private static DefaultListableBeanFactory springFactory;

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        ApplicationContextHelper.setContext(applicationContext);
        if (applicationContext instanceof AbstractRefreshableApplicationContext) {
            AbstractRefreshableApplicationContext springContext = (AbstractRefreshableApplicationContext) applicationContext;
            ApplicationContextHelper.setFactory((DefaultListableBeanFactory) springContext.getBeanFactory());
        } else if (applicationContext instanceof GenericApplicationContext) {
            GenericApplicationContext springContext = (GenericApplicationContext) applicationContext;
            ApplicationContextHelper.setFactory(springContext.getDefaultListableBeanFactory());
        }
    }

    private static void setContext(ApplicationContext applicationContext) {
        ApplicationContextHelper.context = applicationContext;
    }

    private static void setFactory(DefaultListableBeanFactory springFactory) {
        ApplicationContextHelper.springFactory = springFactory;
    }

    public static DefaultListableBeanFactory getSpringFactory() {
        return springFactory;
    }

    public static ApplicationContext getContext() {
        return context;
    }

    /**
     * <ol>
     *     注册单例bean
     *     <li>此种方式注入的单例bean可以通过{@code ApplicationContext#getBean} 获取到单例bean，
     *     但是不能通过{@code beanDefinitionMap} 来获取bean的定义信息，所以如果直接调用
     *     {@link DefaultListableBeanFactory#removeBeanDefinition(String)} 来
     *     移除容器的单例bean时会抛出异常 {@link NoSuchBeanDefinitionException}</li>
     * </ol>
     *
     * @param beanName        beanName 不可与容器里已存在的bean名称重复，否则抛出异常 {@link IllegalStateException}
     * @param singletonObject 单例bean对象
     */
    public static void dynamicRegisterSingletonBean(@NonNull String beanName,
                                                    @NonNull Object singletonObject) {
        // 直接向容器里注入单例bean，如果容器里已经存在名称为beanName的bean，则不允许重复注入，spring会直接抛出异常 IllegalStateException
        springFactory.registerSingleton(beanName, singletonObject);
    }

    /**
     * <ol>
     *     根据beanClass动态添加bean到容器（推荐使用此种方式动态注入）
     *     <li>此种方式添加的bean，在容器中既可以获取到BeanDefinition又可以获取到bean对象</li>
     *     <li>如果容器中有重复的bean（beanName相同），则follow应用的配置（spring.main.allow-bean-definition-overriding）
     *     决定是否需要覆盖容器中的bean</li>
     * </ol>
     *
     * @param beanName        beanName 不可与容器里已存在的bean名称重复
     * @param beanClass       beanClass
     * @param constructValues constructValues
     */
    public static void dynamicRegisterSingleBean(@NonNull String beanName,
                                                 @NonNull Class<?> beanClass,
                                                 Object... constructValues) {
        // 生成bean的定义信息的构建器
        BeanDefinitionBuilder beanDefBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanClass);
        // 构造函数赋值
        for (Object constructValue : constructValues) {
            beanDefBuilder.addConstructorArgValue(constructValue);
        }
        // 生成bean的定义信息
        BeanDefinition beanDefinition = beanDefBuilder.getBeanDefinition();
        // 注册bean到容器
        springFactory.registerBeanDefinition(beanName, beanDefinition);
    }

    /**
     * <p>
     * 按beanName从容器里移除bean
     * 这里需要catch xx异常，因为{@link ApplicationContextHelper#dynamicRegisterSingletonBean(java.lang.String, java.lang.Object)}
     * 注入的单例bean是不会在 {@link DefaultListableBeanFactory#beanDefinitionMap} 中存在的，所以当remove的时候会抛出异常
     * {@link NoSuchBeanDefinitionException}
     * </p>
     *
     * @param beanName beanName
     */
    public static void removeBean(@NonNull String beanName) {
        try {
            // 销毁BeanDefinition和单例池里的bean
            springFactory.removeBeanDefinition(beanName);
        } catch (NoSuchBeanDefinitionException ex) {
            LOGGER.error("removeBean failed, because there is no NoSuchBeanDefinition, beanName is : [{}]", beanName, ex);
            // 直接销毁单例池里的bean
            springFactory.destroySingleton(beanName);
        }
    }

    /**
     * 异步从 ApplicationContextHelper 获取 bean 对象并设置到目标对象中，在某些启动期间需要初始化的bean可采用此方法。
     * <p>
     * 适用于实例方法注入。
     *
     * @param type         bean type
     * @param target       目标类对象
     * @param setterMethod setter 方法，target 中需包含此方法名，且类型与 type 一致
     * @param <T>          type
     */
    public static <T> void asyncInstanceSetter(Class<T> type, Object target, String setterMethod) {
        if (setByMethod(type, target, setterMethod)) {
            return;
        }

        AtomicInteger counter = new AtomicInteger(0);
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1, r -> new Thread(r, "sync-setter"));
        executorService.scheduleAtFixedRate(() -> {
            boolean success = setByMethod(type, target, setterMethod);
            if (success) {
                executorService.shutdown();
            } else {
                if (counter.addAndGet(1) > 240) {
                    LOGGER.error("Setter field [{}] in [{}] failure because timeout.", setterMethod, target.getClass().getName());
                    executorService.shutdown();
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * 异步从 ApplicationContextHelper 获取 bean 对象并设置到目标对象中，在某些启动期间需要初始化的bean可采用此方法。
     * <br>
     * 一般可用于向静态类注入实例对象。
     *
     * @param type        bean type
     * @param target      目标类
     * @param targetField 目标字段
     */
    public static void asyncStaticSetter(Class<?> type, Class<?> target, String targetField) {

        if (setByField(type, target, targetField)) {
            return;
        }

        AtomicInteger counter = new AtomicInteger(0);
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1, r -> new Thread(r, "sync-setter"));

        executorService.scheduleAtFixedRate(() -> {
            boolean success = setByField(type, target, targetField);
            if (success) {
                executorService.shutdown();
            } else {
                if (counter.addAndGet(1) > 240) {
                    LOGGER.error("Setter field [{}] in [{}] failure because timeout.", targetField, target.getName());
                    executorService.shutdown();
                }
            }
        }, 0, 1, TimeUnit.SECONDS);

    }

    private static boolean setByMethod(Class<?> type, Object target, String targetMethod) {
        if (ApplicationContextHelper.getContext() != null) {
            try {
                Object obj = ApplicationContextHelper.getContext().getBean(type);
                Method method = target.getClass().getDeclaredMethod(targetMethod, type);
                method.setAccessible(true);
                method.invoke(target, obj);
                LOGGER.info("Async set field [{}] in [{}] success by method.", targetMethod, target.getClass().getName());
                return true;
            } catch (NoSuchMethodException e) {
                LOGGER.error("Not found method [{}] in [{}].", targetMethod, target.getClass().getName(), e);
            } catch (NoSuchBeanDefinitionException e) {
                LOGGER.error("Not found bean [{}] for [{}].", type.getName(), target.getClass().getName(), e);
            } catch (Exception e) {
                LOGGER.error("Async set field [{}] in [{}] failure by method.", targetMethod, target.getClass().getName(), e);
            }
        }
        return false;
    }

    private static boolean setByField(Class<?> type, Class<?> target, String targetField) {
        if (ApplicationContextHelper.getContext() != null) {
            try {
                Object obj = ApplicationContextHelper.getContext().getBean(type);
                Field field = target.getDeclaredField(targetField);
                field.setAccessible(true);
                field.set(target, obj);
                LOGGER.info("Async set field [{}] in [{}] success by field.", targetField, target.getName());
                return true;
            } catch (NoSuchFieldException e) {
                LOGGER.error("Not found field [{}] in [{}].", targetField, target.getName(), e);
            } catch (NoSuchBeanDefinitionException e) {
                LOGGER.error("Not found bean [{}] for [{}].", type.getName(), target.getName(), e);
            } catch (Exception e) {
                LOGGER.error("Async set field [{}] in [{}] failure by field.", targetField, target.getName(), e);
            }
        }
        return false;
    }

}
