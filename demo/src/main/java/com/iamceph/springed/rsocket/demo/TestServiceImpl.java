package com.iamceph.springed.rsocket.demo;

import com.google.protobuf.Empty;
import com.iamceph.springed.rsocket.starter.service.RSocketService;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RSocketService
public class TestServiceImpl implements TestService {
    @Override
    public Mono<TestOut> one(TestIn message, ByteBuf metadata) {
        return Mono.just(TestOut.newBuilder()
                        .setResp("test")
                        .build())
                .doOnNext(next -> log.debug("Sending one response: {}", next));
    }

    @Override
    public Flux<TestOut> all(Empty message, ByteBuf metadata) {
        return Flux.range(1, 100)
                .map(next -> UUID.randomUUID())
                .map(next -> TestOut.newBuilder().setResp(next.toString()).build())
                .doOnNext(next -> log.debug("Responding to the client: {}", next));
    }
}
