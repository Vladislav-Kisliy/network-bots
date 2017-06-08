package com.karlsoft.telnet.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;

import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;

/**
 * Created by Vladislav Kisliy<vladislav.kisliy@gmail.com> on 07.06.17.
 */
public class ClientAuthHandler extends SimpleChannelInboundHandler<String> {

    private enum ClientState {
        AUTHENTICATING,
        AUTHENTICATED,
    }

    private static final Logger LOG = getLogger(ClientAuthHandler.class.getName());
    private final String user;
    private final String pw;
    private ClientState clientState;

    public ClientAuthHandler(String user, String pw) {
        this.user = user;
        this.pw = pw;
        this.clientState = ClientState.AUTHENTICATING;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        LOG.log(Level.INFO, "Got message {0}", msg);
        if (clientState == ClientState.AUTHENTICATING) {
            if (auth(ctx, msg)) {
                clientState = ClientState.AUTHENTICATED;
                // Change decoder and remove yourself
                ctx.pipeline().removeFirst();
                ctx.pipeline().addFirst(new DelimiterBasedFrameDecoder(8192 * 2, Delimiters.lineDelimiter()));
                ctx.pipeline().remove(this);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.log(Level.SEVERE, "Error {0}", cause);
        ctx.close();
    }
    // Simple authorization procedure
    private boolean auth(ChannelHandlerContext ctx, String msg) {
        String response = null;
        boolean result = false;
        if ("login".equals(msg)) {
            LOG.log(Level.INFO, "Got 1st line");
            response = user + "\r\n";
        } else if ("password".equals(msg)) {
            LOG.log(Level.INFO, "Got 2nd line");
            response = pw + "\r\n";
            result = true;
        }
        if (response != null) {
            LOG.log(Level.INFO, "Send to server {0}", response);
            ctx.writeAndFlush(response);
        }
        return result;
    }
}
