package com.karlsoft.network.telnet.protocol.option;

import com.karlsoft.network.telnet.protocol.TelnetOption;
import com.karlsoft.network.telnet.protocol.setting.TelnetSetting;
import com.karlsoft.network.telnet.transport.HexUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponseDecoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import io.netty.handler.proxy.ProxyConnectException;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.util.NetUtil;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

@Slf4j
public class TelnetOptionHandler extends ProxyHandler {

    public static final String DEFAULT_PROMPT_MESSAGE = ">|>\\s|#|#\\s";
    private static final String PROTOCOL = "telnet";
    private static final String AUTH_PASSWORD = "password";
    private static final String LOGIN_MESSAGE = "sername:|ogin:";
    private static final String PASSWORD_MESSAGE = "assword:|PASSCODE:";
    private static final String SECOND_LOGIN = ".+" + LOGIN_MESSAGE + "|" + DEFAULT_PROMPT_MESSAGE;
    private static final String INCORRECT_LOGIN_CISCO = "Username";
    private static final int PROMPT_ATTEMPTS = 10;

    private final List<TelnetSetting> telnetSettings;
    private final EnumMap<TelnetOption, TelnetOptionNegotiationHandler> negHandlers;
    private String decoderName;
    private String encoderName;

    public TelnetOptionHandler(SocketAddress proxyAddress) {
        this(proxyAddress, Collections.emptyList());
    }

    public TelnetOptionHandler(SocketAddress proxyAddress, List<TelnetSetting> telnetSettings) {
        super(proxyAddress);
        this.telnetSettings = telnetSettings;
        this.negHandlers = new EnumMap<>(TelnetOption.class);
    }

    @Override
    public String protocol() {
        return PROTOCOL;
    }

    @Override
    public String authScheme() {
        return AUTH_PASSWORD;
    }

    @Override
    protected void addCodec(ChannelHandlerContext ctx) throws Exception {
        ChannelPipeline p = ctx.pipeline();
        String name = ctx.name();

        TelnetOptionPacketDecoder decoder = new TelnetOptionPacketDecoder();
        p.addBefore(name, null, decoder);

        decoderName = p.context(decoder).name();
        encoderName = decoderName + ".encoder";
        p.addBefore(name, encoderName, TelnetOptionPacketEncoder.DEFAULT);
    }

    @Override
    protected void removeEncoder(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline().remove(encoderName);
    }

    @Override
    protected void removeDecoder(ChannelHandlerContext ctx) throws Exception {
        ChannelPipeline p = ctx.pipeline();
        if (p.context(decoderName) != null) {
            p.remove(decoderName);
        }
    }

    @Override
    protected Object newInitialMessage(ChannelHandlerContext ctx) throws Exception {
        TelnetOptionPacket packet = null;
//        if (!telnetSettings.isEmpty()) {
//            packet = new DefaultTelnetOptionPacket(TelnetCommand.DO, TelnetOption.SUPPRESS_GO_AHEAD);
//        }
        return packet;
    }

    @Override
    protected boolean handleResponse(ChannelHandlerContext ctx, Object response) throws Exception {
        log.debug("Got response =" + response);
        if (response instanceof TelnetCommandPacket) {
            TelnetCommandPacket res = (TelnetCommandPacket) response;
            System.out.println("res = " + res);
            if (res.getCommand().isNegotiation()) {
                handleOption((TelnetOptionPacket) res);
            } else {
                handleCommand(res);
            }
        } else if (response instanceof ByteBuf) {
            ByteBuf res = (ByteBuf) response;
            String s = res.readCharSequence(res.readableBytes(), StandardCharsets.UTF_8).toString();
            System.out.println("lines =" + s);
            System.out.println("hex lines:");
            HexUtils.debugOutput(s);
//            return true;
        }
        return false;
    }

    private void handleCommand(TelnetCommandPacket res) {
        log.debug("Command {}", res.getCommand());
    }

    private void handleOption(TelnetOptionPacket res) {
        log.debug("Command {}, option {}", res.getCommand(), res.getOption());
        TelnetOptionNegotiationHandler handler = getNegotiationHandler(res);
        sendToProxyServer(handler.getResponse(res));
    }

    private TelnetOptionNegotiationHandler getNegotiationHandler(TelnetOptionPacket res) {
        TelnetOptionNegotiationHandler handler;
        if (negHandlers.containsKey(res.getOption())) {
            handler = negHandlers.get(res.getOption());
            log.debug("Found handler {}, for command {}", handler, res.getCommand());
        } else {
            log.debug("Didn't find handler, for command {}. Will use default", res.getCommand());
            handler = DefaultTelnetOptionNegotiationHandler.DEFAULT;
        }
        return handler;
    }

}
