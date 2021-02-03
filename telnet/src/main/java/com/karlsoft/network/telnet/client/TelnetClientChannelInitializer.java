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

    public TelnetClientChannelInitializer(List<TelnetSetting> telnetSettings) {
        this.telnetSettings = telnetSettings;
    }

    @Override
    public void onChannelInit(ConnectionObserver connectionObserver, Channel channel, SocketAddress remoteAddress) {
        Objects.requireNonNull(channel, "channel");
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addFirst(TELNET_HANDLER, new TelnetOptionHandler(remoteAddress, telnetSettings));

        if (pipeline.get(NettyPipeline.LoggingHandler) != null) {
            pipeline.addBefore(NettyPipeline.ProxyHandler,
                    NettyPipeline.ProxyLoggingHandler,
                    TelnetClientConfig.LOGGING_HANDLER);
        }
    }
}
