package com.karlsoft.network.telnet.client;

import com.karlsoft.network.telnet.client.auth.Credentials;
import com.karlsoft.network.telnet.protocol.newpack.LoginHandler;
import com.karlsoft.network.telnet.protocol.newpack.OptionNegotiationHandler;
import com.karlsoft.network.telnet.protocol.newpack.OptionPacketDecoder;
import com.karlsoft.network.telnet.protocol.option.TelnetOptionHandler;
import com.karlsoft.network.telnet.protocol.option.TelnetOptionPacketEncoder;
import com.karlsoft.network.telnet.protocol.setting.TelnetSetting;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import reactor.netty.ChannelPipelineConfigurer;
import reactor.netty.ConnectionObserver;
import reactor.netty.NettyPipeline;

import java.net.SocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class TelnetClientChannelInitializer implements ChannelPipelineConfigurer {

    public static final String TELNET_HANDLER = "TelnetOptionHandler";
    private static final StringDecoder STRING_DECODER = new StringDecoder();
    private static final StringEncoder STRING_ENCODER = new StringEncoder();

    private final List<TelnetSetting> telnetSettings;
    private final long connectTimeoutMillis;
    private final Credentials creds;

    public TelnetClientChannelInitializer(List<TelnetSetting> telnetSettings, long connectTimeoutMillis,
                                          Credentials creds) {
        this.telnetSettings = telnetSettings;
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.creds = creds;
    }

    @Override
    public void onChannelInit(ConnectionObserver connectionObserver, Channel channel, SocketAddress remoteAddress) {
        Objects.requireNonNull(channel, "channel");
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addFirst("ReadTimeout", new ReadTimeoutHandler(connectTimeoutMillis, TimeUnit.MILLISECONDS));
        pipeline.addFirst("OptionHandler", new OptionPacketDecoder());
        pipeline.addAfter("OptionHandler", "StringDecoder", STRING_DECODER);
        pipeline.addAfter("OptionHandler", "TeletEncoderName", TelnetOptionPacketEncoder.DEFAULT);
        pipeline.addAfter("StringDecoder", "STRING_ENCODER", STRING_ENCODER);
        OptionNegotiationHandler negotiationHandler = new OptionNegotiationHandler();
        pipeline.addAfter("STRING_ENCODER", TELNET_HANDLER, negotiationHandler);
        pipeline.addAfter(TELNET_HANDLER, "LoginHandler", new LoginHandler(creds));

//        TelnetOptionHandler handler = new TelnetOptionHandler(remoteAddress, telnetSettings, creds);
//        handler.setConnectTimeoutMillis(connectTimeoutMillis);
//        pipeline.addFirst(TELNET_HANDLER, handler);


        if (pipeline.get(NettyPipeline.LoggingHandler) != null) {
            pipeline.addBefore(NettyPipeline.ProxyHandler, NettyPipeline.ProxyLoggingHandler,
                    TelnetClientConfig.LOGGING_HANDLER);
        }
    }
}
