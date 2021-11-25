package com.iamceph.springed.rsocket.starter.server;

import reactor.netty.tcp.TcpServer;

import java.util.function.Supplier;

@FunctionalInterface
public interface RSocketTcpServerBuilder {

    /**
     * Callback for TcpServer building.
     */
    void configure(Supplier<TcpServer> builder);
}
