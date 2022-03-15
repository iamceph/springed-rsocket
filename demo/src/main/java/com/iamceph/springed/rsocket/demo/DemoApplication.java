package com.iamceph.springed.rsocket.demo;

import com.iamceph.springed.rsocket.starter.config.EnableRSocketRpc;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@EnableRSocketRpc(basePackages = "com.iamceph.springed.rsocket")
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(DemoApplication.class)
                .registerShutdownHook(true)
                .run(args);
    }
}
