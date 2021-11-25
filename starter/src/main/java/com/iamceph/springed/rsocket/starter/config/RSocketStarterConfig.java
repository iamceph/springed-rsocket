package com.iamceph.springed.rsocket.starter.config;

import com.iamceph.springed.rsocket.starter.service.RSocketService;
import io.rsocket.frame.decoder.PayloadDecoder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@ConfigurationProperties("springed.rsocket")
@Component
@Getter
@Setter
public class RSocketStarterConfig {
    /**
     * If the RSocket server should be enabled or not.
     */
    private Boolean enabled = true;
    /**
     * Name of the server. Used in thread name.
     */
    private String serverName = "RSocket";
    /**
     * If custom service name from {@link RSocketService#customName()} should be applied to all services.
     * This is a hackity way via reflection, use it on your own risk.
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
         * Type of the connection
         */
        private ConnectionType connectionType = ConnectionType.TCP;
        /**
         * How many seconds should we wait before shutting down the server.
         */
        private Integer shutdownWait = 0;
        /**
         * If the {@link io.rsocket.core.Resume} functionality is supported.
         */
        private Boolean supportResume = true;
        /**
         * What PayloadDecoder type should the starter use.
         */
        private PayloadDecoderType payloadDecoderType = PayloadDecoderType.DEFAULT;

        /**
         * Type of the connection
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

        /**
         * Type of the {@link PayloadDecoder}
         */
        public enum PayloadDecoderType {
            /**
             * DEFAULT PayloadDecoder, defined in {@link PayloadDecoder#DEFAULT}
             */
            DEFAULT,
            /**
             * ZERO_COPY PayloadDecoder, defined in {@link PayloadDecoder#ZERO_COPY}
             */
            ZERO_COPY,
            /**
             * CUSTOM PayloadDecoder, you need to define this one as a Bean.
             */
            CUSTOM
        }
    }

    @Getter
    @Setter
    public static class Security {
        /**
         * If the RSocket server should be secured
         */
        private Boolean enabled = false;

        //TODO
    }
}
