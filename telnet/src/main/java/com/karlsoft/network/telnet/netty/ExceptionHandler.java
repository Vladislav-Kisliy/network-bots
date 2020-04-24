package com.karlsoft.network.telnet.netty;

import io.netty.channel.*;
import org.slf4j.Logger;

import java.net.SocketAddress;

import static org.slf4j.LoggerFactory.getLogger;
//import java.util.logging.Logger;

/**
 * Created by Vladislav Kisliy<vkisliy@productengine.com> on 08.06.17.
 */
public class ExceptionHandler extends ChannelDuplexHandler {

    private static final Logger LOG = getLogger(ExceptionHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Uncaught exceptions from inbound handlers will propagate up to this handler
        LOG.error("Exception was caught {}", new Object[] {cause});
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        ctx.connect(remoteAddress, localAddress, promise.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (!future.isSuccess()) {
                    // Handle connect exception here...
                    LOG.error("Can't connect to host {}. Error {}", new Object[] {remoteAddress, future.cause().getMessage()});
                }
            }
        }));
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        ctx.write(msg, promise.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (!future.isSuccess()) {
                    // Handle write exception here...
                    LOG.error("Can't write message {}. Error {}", new Object[] {msg, future.cause()});
                }
            }
        }));
    }
}
