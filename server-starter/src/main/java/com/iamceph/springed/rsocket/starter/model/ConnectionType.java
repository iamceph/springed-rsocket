package com.iamceph.springed.rsocket.starter.model;

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
