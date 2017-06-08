package com.karlsoft.telnet.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * Creates a newly configured {@link ChannelPipeline} for a new channel.
 */
public class TelnetClientInitializer extends ChannelInitializer<SocketChannel> {

    private static final StringDecoder DECODER = new StringDecoder();
    private static final StringEncoder ENCODER = new StringEncoder();

    private static final TelnetClientHandler CLIENT_HANDLER = new TelnetClientHandler();

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        ByteBuf[] delimiters = new ByteBuf[]{
                Unpooled.wrappedBuffer(new byte[]{'\r', '\n'}), // WINDOWS
                Unpooled.wrappedBuffer(new byte[]{'\n'}),       // UNIX / OSX
                Unpooled.wrappedBuffer(new byte[]{':'})        // Login/Password lines
        };

        // Add the text line codec combination first,
        pipeline.addLast(new DelimiterBasedFrameDecoder(8192, delimiters));
        pipeline.addLast(DECODER);
        pipeline.addLast(ENCODER);

        // and then business logic.
        pipeline.addLast(new ClientAuthHandler("root", "1234"));
        pipeline.addLast(CLIENT_HANDLER);
    }
}
