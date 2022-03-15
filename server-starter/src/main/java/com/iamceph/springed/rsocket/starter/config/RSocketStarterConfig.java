package com.iamceph.springed.rsocket.starter.config;

import com.iamceph.springed.rsocket.starter.model.ConnectionType;
import com.iamceph.springed.rsocket.starter.model.PayloadDecoderType;
import com.iamceph.springed.rsocket.starter.service.RSocketService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;

@ConfigurationProperties("springed.rsocket")
@Component
@Getter
@Setter
public class RSocketStarterConfig {
    /**
     * If the RSocket server should be enabled or not.
     */
    private Boolean enabled = false;
    /**
     * Name of the server. Used in thread name.
     */
    private String serverName = "RSocket";
    /**
     * If custom service name from {@link RSocketService#customName()} should be applied to all services.
     * This is a hackity way, it WILL throw "An illegal reflective access operation has occurred" in your logs.
     * Beware and use it on your own risk.
     */
    private Boolean modifyServiceNames = false;
    /**
     * Connection settings
     */
    @NestedConfigurationProperty
    private Connection connection = new Connection();
    /**
     * Security settings
     */
    @NestedConfigurationProperty
    private Security security = new Security();

    @Getter
    @Setter
    public static class Connection {
        /**
         * Port on what RSocket server will run.
         */
        private Integer port = 6969;
        /**
         * Host that RSocket server will run.
         */
        private String host = "localhost";
        /**
         * How many seconds should we wait before shutting down the server.
         */
        private Duration shutdownWait = Duration.ofSeconds(3);
        /**
         * If the {@link io.rsocket.core.Resume} functionality is supported.
         */
        private Boolean supportResume = true;
        /**
         * Which connection type should the starter use.
         */
        private ConnectionType connectionType = ConnectionType.TCP;
        /**
         * What PayloadDecoder type should the starter use.
         */
        private PayloadDecoderType payloadDecoderType = PayloadDecoderType.DEFAULT;
    }

    @Getter
    @Setter
    public static class Security {
        /**
         * If the RSocket server should be secured
         */
        private Boolean enabled = false;

        //TODO: add SSL support
    }
}
