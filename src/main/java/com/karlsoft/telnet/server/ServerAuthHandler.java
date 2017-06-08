package com.karlsoft.telnet.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetAddress;
import java.util.Date;

/**
 * Created by Vladislav Kisliy<vladislav.kisliy@gmail.com> on 07.06.17.
 */
public class ServerAuthHandler extends SimpleChannelInboundHandler<String> {

    private boolean isAuth = false;
    private String user = null;
    private String pw = null;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Send greeting for a new connection.
        ctx.write("Welcome to " + InetAddress.getLocalHost().getHostName() + "!\r\n");
        ctx.write("It is " + new Date() + " now.\r\n");
        ctx.writeAndFlush("login:");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        if (!isAuth) {
            if (auth(ctx, msg)) {
                ctx.write("Welcome!\r\nEnter your commands. Have a fun!\r\n");
                ctx.flush();
                ctx.pipeline().remove(this);
            } else {
                System.out.println("else");
            }
        }
    }

    // Set isAuth to true if auth was successfully
    private boolean auth(ChannelHandlerContext ctx, String msg) {
        System.out.println("auth. message. request =" + msg);
        String response;
        boolean close = false;
        boolean result = false;
        if (msg.isEmpty()) {
            response = "Please type something.\r\n";
        } else if ("bye".equals(msg.toLowerCase())) {
            response = "Have a good day!\r\n";
            close = true;
        } else if (user == null) {
            user = msg;
            response = "password:";
        } else {
            pw = msg;
            if ("root".equals(user) && "1234".equals(pw)) {
                result = true;
                isAuth = true;
                response = "Sucess!\r\n";
            } else {
                user = null;
                pw = null;
                response = "Incorrect login or password. Try again.\r\n" +
                        "login:";
            }
        }
        ChannelFuture future = ctx.write(response);
        // Close the connection after sending 'Have a good day!'
        // if the client has sent 'bye'.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
        ctx.flush();
        return result;
    }
}