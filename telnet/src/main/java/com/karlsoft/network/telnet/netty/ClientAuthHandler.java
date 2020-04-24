package com.karlsoft.network.telnet.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Vladislav Kisliy
 */
public class ClientAuthHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private enum ClientState {
        AUTHENTICATING,
        AUTHENTICATED,
        ERROR
    }

    private static final Logger LOG = LoggerFactory.getLogger(ClientAuthHandler.class);
    private static final byte req_1[] = {
            (byte) 0xff, (byte) 0xfd, 0x18, (byte) 0xff, (byte) 0xfd, 0x20, (byte) 0xff, (byte) 0xfd,
            0x23, (byte) 0xff, (byte) 0xfd, 0x27};

    private static final byte req_2[] = {
            (byte) 0xff, (byte) 0xfb, 0x03, (byte) 0xff, (byte) 0xfd, 0x1f, (byte) 0xff, (byte) 0xfd,
            0x21, (byte) 0xff, (byte) 0xfe, 0x22, (byte) 0xff, (byte) 0xfb, 0x05, (byte) 0xff,
            (byte) 0xfa, 0x20, 0x01, (byte) 0xff, (byte) 0xf0, (byte) 0xff, (byte) 0xfa, 0x23,
            0x01, (byte) 0xff, (byte) 0xf0, (byte) 0xff, (byte) 0xfa, 0x27, 0x01, (byte) 0xff,
            (byte) 0xf0, (byte) 0xff, (byte) 0xfa, 0x18, 0x01, (byte) 0xff, (byte) 0xf0};

    private static final byte req_3[] = {(byte) 0xff, (byte) 0xfd, 0x01};

    private static final byte repl_1[] = {
            (byte) 0xff, (byte) 0xfd, 0x03, (byte) 0xff, (byte) 0xfb, 0x18, (byte) 0xff, (byte) 0xfb,
            0x1f, (byte) 0xff, (byte) 0xfb, 0x20, (byte) 0xff, (byte) 0xfb, 0x21, (byte) 0xff,
            (byte) 0xfb, 0x22, (byte) 0xff, (byte) 0xfb, 0x27, (byte) 0xff, (byte) 0xfd, 0x05,
            (byte) 0xff, (byte) 0xfb, 0x23};
    private static final byte repl_2[] = {
            (byte) 0xff, (byte) 0xfa, 0x1f, 0x00, 0x7b, 0x00, 0x25, (byte) 0xff,
            (byte) 0xf0, (byte) 0xff, (byte) 0xfa, 0x20, 0x00, 0x33, 0x38, 0x34,
            0x30, 0x30, 0x2c, 0x33, 0x38, 0x34, 0x30, 0x30,
            (byte) 0xff, (byte) 0xf0, (byte) 0xff, (byte) 0xfa, 0x23, 0x00, 0x6c, 0x69,
            0x6e, 0x75, 0x78, 0x2d, 0x65, 0x73, 0x75, 0x33,
            0x3a, 0x30, (byte) 0xff, (byte) 0xf0, (byte) 0xff, (byte) 0xfa, 0x27, 0x00,
            0x03, 0x58, 0x41, 0x55, 0x54, 0x48, 0x4f, 0x52,
            0x49, 0x54, 0x59, 0x01, 0x2f, 0x74, 0x6d, 0x70,
            0x2f, 0x78, 0x61, 0x75, 0x74, 0x68, 0x2d, 0x31,
            0x30, 0x30, 0x30, 0x2d, 0x5f, 0x30, 0x00, 0x44,
            0x49, 0x53, 0x50, 0x4c, 0x41, 0x59, 0x01, 0x6c,
            0x69, 0x6e, 0x75, 0x78, 0x2d, 0x65, 0x73, 0x75,
            0x33, 0x3a, 0x30, (byte) 0xff, (byte) 0xf0, (byte) 0xff, (byte) 0xfa, 0x18,
            0x00, 0x58, 0x54, 0x45, 0x52, 0x4d, (byte) 0xff, (byte) 0xf0};


    private static final byte repl_3[] = {(byte) 0xff, (byte) 0xfc, 0x01};

    private final String user;
    private final String pw;
    private final BlockingQueue<String> queue;
    private ClientState clientState;
    private int handShakeCounter = 0;

    public ClientAuthHandler(String user, String pw, BlockingQueue<String> queue) {
        this.user = user;
        this.pw = pw;
        this.queue = queue;
        this.clientState = ClientState.AUTHENTICATING;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        byte[] networkBytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(networkBytes);
        LOG.debug("Got message bytes {} and line {}", networkBytes, byteBuf);
        // First step of handshake
        if (handShakeCounter == 0 && Arrays.equals(networkBytes, req_1)) {
            System.out.println("send repl_1");
            ctx.channel().writeAndFlush(Unpooled.wrappedBuffer(repl_1));

            handShakeCounter++;
        } else if (handShakeCounter == 1 && Arrays.equals(networkBytes, req_2)) {
            System.out.println("send repl_2");
            ctx.channel().writeAndFlush(Unpooled.wrappedBuffer(repl_2));
            handShakeCounter++;
        } else if (handShakeCounter == 2 && Arrays.equals(networkBytes, req_3)) {
            System.out.println("send repl_3");
            ctx.channel().writeAndFlush(Unpooled.wrappedBuffer(repl_3));
            handShakeCounter++;
        } else if (handShakeCounter == 3) {
            String loginMessage = new String(networkBytes).trim().toLowerCase();
            if (loginMessage.contains("login")) {
                System.out.println("send login");
                ctx.channel().writeAndFlush(Unpooled.wrappedBuffer((user + "\r\n").getBytes()));
                handShakeCounter++;
            }
        } else if (handShakeCounter == 4) {
            String loginMessage = new String(networkBytes).trim().toLowerCase();
            if (loginMessage.contains("password")) {
                System.out.println("send password");
                ctx.channel().writeAndFlush(Unpooled.wrappedBuffer((pw + "\r\n").getBytes()));
                handShakeCounter++;
            }
        }
        if (handShakeCounter == 5) {
            System.out.println("remove yourself");
            ctx.pipeline().remove(this);
            queue.put("SUCCESS");
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error("Error {}", cause);
        ctx.close();
    }

    // Simple authorization procedure
    private boolean auth(ChannelHandlerContext ctx, String msg) {
        String response = null;
        boolean result = false;
        LOG.info("Auth steps. got msg {}", msg);
        if ("login".equals(msg)) {
            response = user + "\r\n";
        } else if ("password".equals(msg)) {
            response = pw + "\r\n";
            result = true;
        }
        if (response != null) {
            LOG.debug("Send to server {}", response);
            ctx.writeAndFlush(response);
        }
        return result;
    }
}
