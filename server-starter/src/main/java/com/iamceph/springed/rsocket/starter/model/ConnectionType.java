package com.iamceph.springed.rsocket.starter.model;

/**
 * A connection type.
 */
public enum ConnectionType {
    TCP,
    WEBSOCKET;

    public boolean isTcp() {
        return this == TCP;
    }

    public boolean isWebsocket() {
        return this == WEBSOCKET;
    }
}
