package com.xiaoxin.pan.lock.core.annotation;

import com.xiaoxin.pan.lock.core.key.KeyGenerator;
import com.xiaoxin.pan.lock.core.key.StandardKeyGenerator;

import java.lang.annotation.*;

/**
 * lock注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Lock {
    String name() default "";
    long expireSeconds() default 60L;
    String[] keys() default {};
    Class<? extends KeyGenerator> keyGenerator() default StandardKeyGenerator.class;
}
