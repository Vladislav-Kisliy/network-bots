package com.karlsoft.network.telnet.client;

import com.karlsoft.network.telnet.client.auth.Credentials;
import com.karlsoft.network.telnet.protocol.setting.TelnetSetting;
import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LoggingHandler;
import reactor.core.publisher.Mono;
import reactor.netty.ChannelPipelineConfigurer;
import reactor.netty.Connection;
import reactor.netty.channel.ChannelMetricsRecorder;
import reactor.netty.channel.ChannelOperations;
import reactor.netty.channel.MicrometerChannelMetricsRecorder;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.TcpResources;
import reactor.netty.transport.ClientTransportConfig;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class TelnetClientConfig extends ClientTransportConfig<TelnetClientConfig> {

    public static final LoggingHandler LOGGING_HANDLER = new LoggingHandler(TelnetClient.class);

    boolean promptDetectionEnabled = false;
    Credentials credentials;
    List<TelnetSetting> telnetSettings;
    long telnetSessionTimeout;
    Function<? super Mono<? extends Connection>, ? extends Mono<? extends Connection>> connector;
    String command;
    Function<Mono<TelnetClientConfig>, Mono<TelnetClientConfig>> deferredConf;


    private static final ChannelOperations.OnSetup DEFAULT_OPS = (ch, c, msg) -> new ChannelOperations<>(ch, c);

    TelnetClientConfig(ConnectionProvider connectionProvider, Map<ChannelOption<?>, ?> options,
                       Supplier<? extends SocketAddress> remoteAddress) {
        super(connectionProvider, options, remoteAddress);
    }

    TelnetClientConfig(TelnetClientConfig parent) {
        super(parent);
        this.command = parent.command;
        this.credentials = parent.credentials;
        this.telnetSettings = parent.telnetSettings;
        this.telnetSessionTimeout = parent.telnetSessionTimeout;
        this.promptDetectionEnabled = parent.promptDetectionEnabled;
        this.connector = parent.connector;
    }

    @Override
    public ChannelOperations.OnSetup channelOperationsProvider() {
        return DEFAULT_OPS;
    }

    @Override
    protected LoggingHandler defaultLoggingHandler() {
        return LOGGING_HANDLER;
    }

    @Override
    protected LoopResources defaultLoopResources() {
        return TcpResources.get();
    }

    @Override
    protected ChannelMetricsRecorder defaultMetricsRecorder() {
        return TelnetClientConfig.MicrometerTcpClientMetricsRecorder.INSTANCE;
    }

    @Override
    protected ChannelPipelineConfigurer defaultOnChannelInit() {
        return super.defaultOnChannelInit()
                .then((new TelnetClientChannelInitializer(telnetSettings, telnetSessionTimeout, credentials)));
    }

    static final class MicrometerTcpClientMetricsRecorder extends MicrometerChannelMetricsRecorder {

        static final MicrometerTcpClientMetricsRecorder INSTANCE = new TelnetClientConfig.MicrometerTcpClientMetricsRecorder();

        MicrometerTcpClientMetricsRecorder() {
            super(reactor.netty.Metrics.TCP_CLIENT_PREFIX, "telnet");
        }
    }

    void deferredConf(Function<TelnetClientConfig, Mono<TelnetClientConfig>> deferrer) {
        if (deferredConf != null) {
            deferredConf = deferredConf.andThen(deferredConf -> deferredConf.flatMap(deferrer));
        } else {
            deferredConf = deferredConf -> deferredConf.flatMap(deferrer);
        }
    }
}
