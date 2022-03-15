package com.iamceph.springed.rsocket.starter.config;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the annotated class as a RSocket service that should be automatically registered.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(EnableRSocketRpcRegistrar.class)
public @interface EnableRSocketRpc {

    @AliasFor(attribute = "basePackages")
    String[] value() default {};

    @AliasFor(attribute = "value")
    String[] basePackages() default {};
}
