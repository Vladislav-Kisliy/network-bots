package com.karlsoft.network.telnet.client;

import com.karlsoft.network.telnet.client.auth.Credentials;
import com.karlsoft.network.telnet.protocol.TelnetOptionHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.resolver.AddressResolverGroup;
import reactor.core.publisher.Mono;
import reactor.netty.ChannelPipelineConfigurer;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.NettyPipeline;
import reactor.netty.channel.ChannelMetricsRecorder;
import reactor.netty.channel.ChannelOperations;
import reactor.netty.channel.MicrometerChannelMetricsRecorder;
import reactor.netty.http.client.HttpClientConfig;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.TcpClientConfig;
import reactor.netty.tcp.TcpResources;
import reactor.netty.transport.ClientTransportConfig;
import reactor.netty.transport.NameResolverProvider;
import reactor.netty.transport.ProxyProvider;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public final class TelnetClientConfig extends ClientTransportConfig<TelnetClientConfig> {

    boolean promptDetectionEnabled = false;
    Credentials credentials;
    Function<? super Mono<? extends Connection>, ? extends Mono<? extends Connection>> connector;
    String command;
    Function<Mono<TelnetClientConfig>, Mono<TelnetClientConfig>> deferredConf;


    private static final ChannelOperations.OnSetup DEFAULT_OPS = (ch, c, msg) -> new ChannelOperations<>(ch, c);

    private static final AddressResolverGroup<?> DEFAULT_RESOLVER =
            NameResolverProvider.builder().build().newNameResolverGroup(TcpResources.get(), LoopResources.DEFAULT_NATIVE);

    private static final LoggingHandler LOGGING_HANDLER = new LoggingHandler(TelnetClient.class);

    TelnetClientConfig(ConnectionProvider connectionProvider, Map<ChannelOption<?>, ?> options,
                       Supplier<? extends SocketAddress> remoteAddress) {
        super(connectionProvider, options, remoteAddress);
    }

    TelnetClientConfig(TelnetClientConfig parent) {
        super(parent);
        this.command = parent.command;
        this.credentials = parent.credentials;
        this.promptDetectionEnabled = parent.promptDetectionEnabled;
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
        return super.defaultOnChannelInit().then((new TelnetClientChannelInitializer()));
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

    static final class TelnetClientChannelInitializer implements ChannelPipelineConfigurer {

        @Override
        public void onChannelInit(ConnectionObserver connectionObserver, Channel channel, SocketAddress remoteAddress) {
            Objects.requireNonNull(channel, "channel");
            ChannelPipeline pipeline = channel.pipeline();
            pipeline.addFirst(NettyPipeline.ProxyHandler, new TelnetOptionHandler(remoteAddress));

            if (pipeline.get(NettyPipeline.LoggingHandler) != null) {
                pipeline.addBefore(NettyPipeline.ProxyHandler,
                        NettyPipeline.ProxyLoggingHandler,
                        LOGGING_HANDLER);
            }
        }
    }
}
