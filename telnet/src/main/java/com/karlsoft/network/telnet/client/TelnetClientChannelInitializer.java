package com.karlsoft.network.telnet.client;

import com.karlsoft.network.telnet.client.auth.Credentials;
import com.karlsoft.network.telnet.protocol.TelnetOption;
import com.karlsoft.network.telnet.protocol.decoder.OptionPacketDecoder;
import com.karlsoft.network.telnet.protocol.encoder.TelnetOptionPacketEncoder;
import com.karlsoft.network.telnet.protocol.handler.LoginHandler;
import com.karlsoft.network.telnet.protocol.handler.OptionNegotiationHandler;
import com.karlsoft.network.telnet.protocol.handler.TelnetOptionNegotiationHandler;
import com.karlsoft.network.telnet.protocol.handler.WindowNegotiationHandler;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class TelnetClientChannelInitializer implements ChannelPipelineConfigurer {

    public static final String TELNET_HANDLER = "TelnetOptionHandler";
    private static final StringDecoder STRING_DECODER = new StringDecoder();
    private static final StringEncoder STRING_ENCODER = new StringEncoder();

    private final List<TelnetSetting> telnetSettings;
    private final long telnetSessionTimeout;
    private final Credentials creds;

    public TelnetClientChannelInitializer(List<TelnetSetting> telnetSettings, long telnetSessionTimeout,
                                          Credentials creds) {
        this.telnetSettings = telnetSettings;
        this.telnetSessionTimeout = telnetSessionTimeout;
        this.creds = creds;
    }

    @Override
    public void onChannelInit(ConnectionObserver connectionObserver, Channel channel, SocketAddress remoteAddress) {
        Objects.requireNonNull(channel, "channel");
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addFirst("ReadTimeout", new ReadTimeoutHandler(telnetSessionTimeout, TimeUnit.MILLISECONDS));
        pipeline.addFirst("OptionHandler", new OptionPacketDecoder());
        pipeline.addAfter("OptionHandler", "StringDecoder", STRING_DECODER);
        pipeline.addAfter("OptionHandler", "TeletEncoderName", TelnetOptionPacketEncoder.DEFAULT);
        pipeline.addAfter("StringDecoder", "STRING_ENCODER", STRING_ENCODER);
        EnumMap<TelnetOption, TelnetOptionNegotiationHandler> negHandlers = getNegotiationHandler();
        OptionNegotiationHandler negotiationHandler = new OptionNegotiationHandler(negHandlers);
        pipeline.addAfter("STRING_ENCODER", TELNET_HANDLER, negotiationHandler);
        pipeline.addAfter(TELNET_HANDLER, "LoginHandler", new LoginHandler(creds));

        if (pipeline.get(NettyPipeline.LoggingHandler) != null) {
            pipeline.addBefore(NettyPipeline.ProxyHandler, NettyPipeline.ProxyLoggingHandler,
                    TelnetClientConfig.LOGGING_HANDLER);
        }
        sendInitialNegotiationSequence(channel);
    }

    private EnumMap<TelnetOption, TelnetOptionNegotiationHandler> getNegotiationHandler() {
        EnumMap<TelnetOption, TelnetOptionNegotiationHandler> map = new EnumMap<>(TelnetOption.class);
        for (TelnetSetting setting : telnetSettings) {
            TelnetOption option = setting.getTelnetOption();
            switch (option) {
                case WINDOW_SIZE:
                    map.put(option, new WindowNegotiationHandler(setting));
                    break;
//                case TERMINAL_TYPE: map.put(option, new )
            }
        }
        return map;
    }

    private void sendInitialNegotiationSequence(Channel channel) {
    }
}
