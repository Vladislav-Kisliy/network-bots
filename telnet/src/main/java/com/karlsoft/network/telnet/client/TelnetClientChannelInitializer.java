package com.karlsoft.network.telnet.client;

import com.karlsoft.network.telnet.protocol.option.TelnetOptionHandler;
import com.karlsoft.network.telnet.protocol.setting.TelnetSetting;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import reactor.netty.ChannelPipelineConfigurer;
import reactor.netty.ConnectionObserver;
import reactor.netty.NettyPipeline;

import java.net.SocketAddress;
import java.util.List;
import java.util.Objects;

public final class TelnetClientChannelInitializer implements ChannelPipelineConfigurer {

    public static final String TELNET_HANDLER = "TelnetOptionHandler";

    private final List<TelnetSetting> telnetSettings;
    private final long connectTimeoutMillis;

    public TelnetClientChannelInitializer(List<TelnetSetting> telnetSettings, long connectTimeoutMillis) {
        this.telnetSettings = telnetSettings;
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    @Override
    public void onChannelInit(ConnectionObserver connectionObserver, Channel channel, SocketAddress remoteAddress) {
        Objects.requireNonNull(channel, "channel");
        ChannelPipeline pipeline = channel.pipeline();
        TelnetOptionHandler handler = new TelnetOptionHandler(remoteAddress, telnetSettings);
        handler.setConnectTimeoutMillis(connectTimeoutMillis);
        pipeline.addFirst(TELNET_HANDLER, handler);

        if (pipeline.get(NettyPipeline.LoggingHandler) != null) {
            pipeline.addBefore(NettyPipeline.ProxyHandler, NettyPipeline.ProxyLoggingHandler,
                    TelnetClientConfig.LOGGING_HANDLER);
        }
    }
}
