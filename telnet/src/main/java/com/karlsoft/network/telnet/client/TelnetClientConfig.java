package com.karlsoft.network.telnet.client;

import com.karlsoft.network.telnet.client.auth.Credentials;
import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LoggingHandler;
import io.netty.resolver.AddressResolverGroup;
import reactor.netty.ChannelPipelineConfigurer;
import reactor.netty.channel.ChannelMetricsRecorder;
import reactor.netty.channel.ChannelOperations;
import reactor.netty.channel.MicrometerChannelMetricsRecorder;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.TcpResources;
import reactor.netty.transport.ClientTransportConfig;
import reactor.netty.transport.NameResolverProvider;

import java.net.SocketAddress;
import java.util.Map;
import java.util.function.Supplier;

public final class TelnetClientConfig extends ClientTransportConfig<TelnetClientConfig> {

    boolean autoDetectPrompt = true;
    Credentials credentials;

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
        return super.defaultOnChannelInit();
    }

    static final class MicrometerTcpClientMetricsRecorder extends MicrometerChannelMetricsRecorder {

        static final MicrometerTcpClientMetricsRecorder INSTANCE = new TelnetClientConfig.MicrometerTcpClientMetricsRecorder();

        MicrometerTcpClientMetricsRecorder() {
            super(reactor.netty.Metrics.TCP_CLIENT_PREFIX, "telnet");
        }
    }

}
