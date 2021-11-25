package com.iamceph.springed.rsocket.starter.server;

import io.rsocket.transport.netty.server.WebsocketServerTransport;

import java.util.function.Supplier;

@FunctionalInterface
public interface RSocketWebsocketServerBuilder {

    /**
     * Custom builder for WebsocketServer
     */
    Supplier<WebsocketServerTransport> configure();
}
