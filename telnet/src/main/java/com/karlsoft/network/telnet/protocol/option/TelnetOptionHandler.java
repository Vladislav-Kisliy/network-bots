package com.karlsoft.network.telnet.protocol.option;

import com.karlsoft.network.telnet.protocol.TelnetCommand;
import com.karlsoft.network.telnet.protocol.TelnetOption;
import com.karlsoft.network.telnet.protocol.setting.TelnetSetting;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponseDecoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.proxy.ProxyConnectException;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.util.NetUtil;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

@Slf4j
public class TelnetOptionHandler extends ProxyHandler {

    private static final String PROTOCOL = "telnet";
    private static final String AUTH_PASSWORD = "password";
    private static final StringDecoder STRING_DECODER = new StringDecoder();
    private static final StringEncoder STRING_ENCODER = new StringEncoder();

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
        p.addLast(STRING_DECODER);
        p.addLast(STRING_ENCODER);
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
        if (!telnetSettings.isEmpty()) {
            packet = new DefaultTelnetOptionPacket(TelnetCommand.DO, TelnetOption.SUPPRESS_GO_AHEAD);
        }
        return packet;
    }

    @Override
    protected boolean handleResponse(ChannelHandlerContext ctx, Object response) throws Exception {
        log.debug("Got response =" + response);
        if (response instanceof TelnetOptionPacket) {
            TelnetOptionPacket res = (TelnetOptionPacket) response;
            log.debug("Command {}, option {}", res.getCommand(), res.getOption());
            TelnetOptionNegotiationHandler handler = getNegotiationHandler(res);
            sendToProxyServer(handler.getResponse(res));
        }
//        if (response instanceof Socks5InitialResponse) {
//            Socks5InitialResponse res = (Socks5InitialResponse) response;
//            Socks5AuthMethod authMethod = socksAuthMethod();
//
//            if (res.authMethod() != Socks5AuthMethod.NO_AUTH && res.authMethod() != authMethod) {
//                // Server did not allow unauthenticated access nor accept the requested authentication scheme.
//                throw new ProxyConnectException(exceptionMessage("unexpected authMethod: " + res.authMethod()));
//            }
//
//            if (authMethod == Socks5AuthMethod.NO_AUTH) {
//                sendConnectCommand(ctx);
//            } else if (authMethod == Socks5AuthMethod.PASSWORD) {
//                // In case of password authentication, send an authentication request.
//                ctx.pipeline().replace(decoderName, decoderName, new Socks5PasswordAuthResponseDecoder());
//                sendToProxyServer(new DefaultSocks5PasswordAuthRequest(
//                        username != null? username : "", password != null? password : ""));
//            } else {
//                // Should never reach here.
//                throw new Error();
//            }
//
//            return false;
//        }
//
//        if (response instanceof Socks5PasswordAuthResponse) {
//            // Received an authentication response from the server.
//            Socks5PasswordAuthResponse res = (Socks5PasswordAuthResponse) response;
//            if (res.status() != Socks5PasswordAuthStatus.SUCCESS) {
//                throw new ProxyConnectException(exceptionMessage("authStatus: " + res.status()));
//            }
//
//            sendConnectCommand(ctx);
//            return false;
//        }
//
//        // This should be the last message from the server.
//        Socks5CommandResponse res = (Socks5CommandResponse) response;
//        if (res.status() != Socks5CommandStatus.SUCCESS) {
//            throw new ProxyConnectException(exceptionMessage("status: " + res.status()));
//        }
//
//        return true;
//
        return false;
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


    private void sendConnectCommand(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress raddr = destinationAddress();
        Socks5AddressType addrType;
        String rhost;
        if (raddr.isUnresolved()) {
            addrType = Socks5AddressType.DOMAIN;
            rhost = raddr.getHostString();
        } else {
            rhost = raddr.getAddress().getHostAddress();
            if (NetUtil.isValidIpV4Address(rhost)) {
                addrType = Socks5AddressType.IPv4;
            } else if (NetUtil.isValidIpV6Address(rhost)) {
                addrType = Socks5AddressType.IPv6;
            } else {
                throw new ProxyConnectException(
                        exceptionMessage("unknown address type: " + StringUtil.simpleClassName(rhost)));
            }
        }

        ctx.pipeline().replace(decoderName, decoderName, new Socks5CommandResponseDecoder());
        sendToProxyServer(new DefaultSocks5CommandRequest(Socks5CommandType.CONNECT, addrType, rhost, raddr.getPort()));
    }
}
