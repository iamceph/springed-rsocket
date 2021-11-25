package com.iamceph.springed.rsocket.demo;

import com.iamceph.springed.rsocket.starter.config.RSocketStarterConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.stereotype.Service;

@SpringBootApplication(scanBasePackages = "com.iamceph.springed.rsocket.demo")
public class DemoApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(DemoApplication.class)
                .registerShutdownHook(true)
                .run(args);
    }

    @Service
    public static class Test {
        private final RSocketStarterConfig config;

        @Autowired
        public Test(RSocketStarterConfig config) {
            this.config = config;
        }
    }
}
