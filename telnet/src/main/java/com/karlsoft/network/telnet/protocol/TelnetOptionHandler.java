package com.karlsoft.network.telnet.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthRequest;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5ClientEncoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponseDecoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponseDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponse;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponseDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthStatus;
import io.netty.handler.proxy.ProxyConnectException;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.util.NetUtil;
import io.netty.util.internal.StringUtil;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class TelnetOptionHandler extends ProxyHandler {

    private static final byte repl_1[] = {
            (byte) 0xff, (byte) 0xfd, 0x03, (byte) 0xff, (byte) 0xfb, 0x18, (byte) 0xff, (byte) 0xfb,
            0x1f, (byte) 0xff, (byte) 0xfb, 0x20, (byte) 0xff, (byte) 0xfb, 0x21, (byte) 0xff,
            (byte) 0xfb, 0x22, (byte) 0xff, (byte) 0xfb, 0x27, (byte) 0xff, (byte) 0xfd, 0x05,
            (byte) 0xff, (byte) 0xfb, 0x23};

    private static final String PROTOCOL = "telnet";
    private static final String AUTH_PASSWORD = "password";

    private final String username;
    private final String password;

    private String decoderName;
    private String encoderName;

    public TelnetOptionHandler(SocketAddress proxyAddress) {
        this(proxyAddress, null, null);
    }

    public TelnetOptionHandler(SocketAddress proxyAddress, String username, String password) {
        super(proxyAddress);
        if (username != null && username.isEmpty()) {
            username = null;
        }
        if (password != null && password.isEmpty()) {
            password = null;
        }
        this.username = username;
        this.password = password;
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

        TelnetInitialResponseDecoder decoder = new TelnetInitialResponseDecoder();
        p.addBefore(name, null, decoder);

        decoderName = p.context(decoder).name();
        encoderName = decoderName + ".encoder";

        p.addBefore(name, encoderName, Socks5ClientEncoder.DEFAULT);
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
        System.out.println("sent a new initial message");
        return Unpooled.wrappedBuffer(repl_1);
    }

    @Override
    protected boolean handleResponse(ChannelHandlerContext ctx, Object response) throws Exception {
        System.out.println("Got response =" + response);
        if (response instanceof TelnetInitialResponse) {
            TelnetInitialResponse res = (TelnetInitialResponse) response;
            System.out.println("Command ="+res.getCommand());
            System.out.println("Option ="+res.getOption());
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
