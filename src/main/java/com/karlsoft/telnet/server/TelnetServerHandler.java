package com.karlsoft.telnet.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Handles a server-side channel.
 */
@Sharable
public class TelnetServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Send greeting for a new connection.
//        ctx.write("Welcome to " + InetAddress.getLocalHost().getHostName() + "!\r\n");
//        ctx.write("It is " + new Date() + " now.\r\n");
        ctx.write("Active TelnetServerHandler.");
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String request) {
        // Generate and write a response.
        System.out.println("TelnetServerHandler. get ="+request);
        String response;
        boolean close = false;
        if (request.isEmpty()) {
            response = "Please type something.\r\n";
        } else if ("bye".equals(request.toLowerCase())) {
            response = "Have a good day!\r\n";
            close = true;
        } else {
            Process myProcess = null;
            int exitVal = -255;
            StringBuilder strBuff = new StringBuilder("Did you say '" + request + "'?\r\n");
            try {
                myProcess = Runtime.getRuntime().exec(request);
                exitVal = myProcess.waitFor();
                BufferedReader input = new BufferedReader(new InputStreamReader(myProcess.getInputStream()));
                String line;
                while ((line = input.readLine()) != null) {
                    strBuff.append(line);
                    strBuff.append("\r\n");
                    System.out.println(line);
                }
            } catch (IOException|InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Exited with error code " + exitVal);
            response = strBuff.toString();
        }
        // We do not need to write a ChannelBuffer here.
        // We know the encoder inserted at TelnetPipelineFactory will do the conversion.
        ChannelFuture future = ctx.write(response);
        // Close the connection after sending 'Have a good day!'
        // if the client has sent 'bye'.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
