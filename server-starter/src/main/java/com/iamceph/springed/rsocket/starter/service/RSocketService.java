package com.iamceph.springed.rsocket.starter.service;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * Maks the annotated class as a RSocket service that should be automatically registered.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface RSocketService {

    /**
     * Represents a custom name for this service.
     *
     * @return A custom name. If empty, class name is used.
     */
    String customName() default "";
}
