package com.iamceph.springed.rsocket.starter.builder;

import io.rsocket.core.RSocketServer;

import java.util.function.Supplier;

@FunctionalInterface
public interface RSocketServerBuilder {

    /**
     * Callback for Server building.
     */
    void configure(Supplier<RSocketServer> builder);
}
