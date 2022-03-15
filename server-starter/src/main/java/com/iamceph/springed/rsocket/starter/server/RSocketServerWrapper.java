package com.iamceph.springed.rsocket.starter.server;

import com.iamceph.resulter.core.DataResultable;
import com.iamceph.springed.rsocket.starter.builder.RSocketServerBuilder;
import com.iamceph.springed.rsocket.starter.builder.RSocketTcpServerBuilder;
import com.iamceph.springed.rsocket.starter.builder.RSocketWebsocketServerBuilder;
import com.iamceph.springed.rsocket.starter.condition.RSocketEnabledCondition;
import com.iamceph.springed.rsocket.starter.config.RSocketStarterConfig;
import com.iamceph.springed.rsocket.starter.service.RSocketServicesManager;
import io.rsocket.core.RSocketServer;
import io.rsocket.core.Resume;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.ipc.RequestHandlingRSocket;
import io.rsocket.rpc.AbstractRSocketService;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.transport.netty.server.WebsocketServerTransport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpResources;
import reactor.netty.tcp.TcpServer;

import java.util.Optional;

@Component
@Conditional(RSocketEnabledCondition.class)
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RSocketServerWrapper implements SmartLifecycle {

    private final ApplicationContext context;
    private final RSocketStarterConfig config;
    private final RSocketServicesManager serviceManager;
    private final Optional<PayloadDecoder> customDecoder;

    private final Optional<RSocketServerBuilder> customRSocketBuilder;
    private final Optional<RSocketTcpServerBuilder> customTcpServerBuilder;
    private final Optional<RSocketWebsocketServerBuilder> customWebsocketBuilder;

    private final Optional<Resume> customResume;

    private CloseableChannel rSocketServer;

    @Override
    public void start() {
        if (isRunning()) {
            log.debug("Cannot start RSocket server again, it is running already!");
            return;
        }

        log.info("Building RSocket server..");

        final var buildResult = buildServer();
        if (buildResult.isFail()) {
            throw new UnsupportedOperationException("Failed to start RSocket server - " + buildResult.message());
        }

        this.rSocketServer = buildResult.data();

        //this will prevent the application from shutting down. :)
        final var waitingThread = new Thread(() -> {
            final var address = rSocketServer.address();
            log.info("RSocket server started, running on [{}]", address);

            rSocketServer.onClose()
                    .onErrorResume(ex -> {
                        log.warn("Exception caught in RSocket onClose!", ex);
                        return Mono.empty();
                    })
                    .block();
        });
        waitingThread.setName(config.getServerName());
        waitingThread.setDaemon(false);
        waitingThread.start();
    }

    @Override
    public void stop() {
        if (!isRunning()) {
            log.debug("RSocket server is not running, cannot stop!");
        }

        log.info("Stopping RSocket server.");
        rSocketServer.dispose();
    }

    @Override
    public boolean isRunning() {
        if (rSocketServer == null) {
            return false;
        }
        return !rSocketServer.isDisposed();
    }

    private DataResultable<CloseableChannel> buildServer() {
        final var serverBuilder = RSocketServer.create();
        //TODO
        final var socketAcceptor = new RequestHandlingRSocket();

        context.getBeansOfType(AbstractRSocketService.class)
                .values()
                .forEach(service -> {
                    log.debug("Registering new service as endpoint for RSocket: {}", service.getClass().getSimpleName());
                    socketAcceptor.withEndpoint(service);
                });

        serverBuilder.acceptor(((setup, sendingSocket) -> Mono.just(socketAcceptor)));

        switch (config.getConnection().getPayloadDecoderType()) {
            case CUSTOM: {
                if (!customDecoder.isPresent()) {
                    log.warn("CUSTOM PayloadDecoder is not present, using DEFAULT!");
                    serverBuilder.payloadDecoder(PayloadDecoder.DEFAULT);
                    break;
                }
                serverBuilder.payloadDecoder(customDecoder.get());
                break;
            }
            case DEFAULT:
                serverBuilder.payloadDecoder(PayloadDecoder.DEFAULT);
                break;
            case ZERO_COPY:
                serverBuilder.payloadDecoder(PayloadDecoder.ZERO_COPY);
                break;
        }

        if (config.getConnection().getConnectionType().isTcp()) {
            log.debug("Building TCPServer for RSocket..");
            return buildTcpServer(serverBuilder);
        }

        log.debug("Building Websocket for RSocket..");
        return buildWebsocketServer(serverBuilder);
    }

    /**
     * Tries to build TCP server.
     *
     * @param rsocketServer builder of RSocket server.
     * @return {@link DataResultable}.
     */
    private DataResultable<CloseableChannel> buildTcpServer(RSocketServer rsocketServer) {
        try {
            final var connection = config.getConnection();
            final var connectionProvider = ConnectionProvider.builder(config.getServerName()).build();
            final var tcpServer = TcpServer.create()
                    .host(connection.getHost())
                    .port(connection.getPort())
                    .runOn(TcpResources.set(connectionProvider));

            if (!config.getSecurity().getEnabled()) {
                log.debug("Disabling SSL, security is disabled.");
                tcpServer.noSSL();
            }

            if (connection.getSupportResume()) {
                rsocketServer.resume(new Resume());
            }

            customTcpServerBuilder.ifPresent(next -> next.configure(() -> tcpServer));
            customRSocketBuilder.ifPresent(next -> next.configure(() -> rsocketServer));

            final var builtServer = rsocketServer.bind(TcpServerTransport.create(tcpServer))
                    .doOnError(ex -> log.warn("Exception while creating RSocket server - {}", ex.getMessage(), ex))
                    .block();

            return DataResultable.failIfNull(builtServer);
        } catch (Exception e) {
            return DataResultable.fail(e);
        }
    }

    /**
     * Tries to build Websocket server.
     *
     * @param rsocketServer builder of RSocket server.
     * @return {@link DataResultable}.
     */
    private DataResultable<CloseableChannel> buildWebsocketServer(RSocketServer rsocketServer) {
        customRSocketBuilder.ifPresent(next -> next.configure(() -> rsocketServer));

        if (customWebsocketBuilder.isPresent()) {
            final var customData = customWebsocketBuilder.get().configure();
            final var builtServer = rsocketServer.bind(customData.get())
                    .doOnError(ex -> log.warn("Exception while creating RSocket server - {}", ex.getMessage(), ex))
                    .block();

            return DataResultable.failIfNull(builtServer);
        }

        final var connection = config.getConnection();
        final var websocketServerTransport = WebsocketServerTransport.create(connection.getHost(), connection.getPort());

        final var builtServer = rsocketServer.bind(websocketServerTransport)
                .doOnError(ex -> log.warn("Exception while creating RSocket server - {}", ex.getMessage(), ex))
                .block();

        return DataResultable.failIfNull(builtServer);
    }
}
