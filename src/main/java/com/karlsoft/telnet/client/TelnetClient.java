package com.karlsoft.telnet.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Simplistic telnet client.
 */
public final class TelnetClient {
    private static final Logger LOG = Logger.getLogger(TelnetClient.class.getName());

    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = 8023;

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new TelnetClientInitializer());
            // Start the connection attempt.
            Channel ch = b.connect(HOST, PORT).sync().channel();
            // Read commands from the stdin.
            ChannelFuture lastWriteFuture = null;
            String[] commands = new String[]{"ls -lah", "w", "ls -lah /"};
            for (String command : commands) {
                System.out.println("one tick");
                TimeUnit.SECONDS.sleep(1);
                System.out.println("send =" + command);
                lastWriteFuture = ch.writeAndFlush(command + "\r\n");
            }
            // Get last command output
            System.out.println("last tick");
            TimeUnit.SECONDS.sleep(1);
            // Wait until all messages are flushed before closing the channel.
            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }
        } finally {
            group.shutdownGracefully();
        }
    }
}
