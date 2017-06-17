/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.karlsoft.network.telnet.netty;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * Handles a client-side channel.
 */
@Sharable
public class TelnetClientHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger LOG = LoggerFactory.getLogger(TelnetClientHandler.class);
    private static final int DEFAULT_REFRESH_TIMER = 300;
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);
    private final BlockingQueue<String> queue;
    private ScheduledFuture<?> scheduleTask;
    private StringBuilder outputBuilder = new StringBuilder();

    public TelnetClientHandler(BlockingQueue queue) {
        this.queue = queue;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        outputBuilder.append(msg);
        outputBuilder.append("\r\n");
        updateTimer();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error("Error ", cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        scheduledExecutor.shutdown();
        scheduledExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        super.channelInactive(ctx);
        scheduledExecutor.shutdownNow();
        LOG.debug("Deactivated channel successfully");
    }

    public void updateTimer() {
        if (scheduleTask != null) {
            scheduleTask.cancel(true);
        }
        scheduleTask = scheduledExecutor.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    queue.offer(outputBuilder.toString(), 1, TimeUnit.SECONDS);
                    outputBuilder = new StringBuilder();
                } catch (InterruptedException ex) {
                    LOG.warn("Interrupted task. Exception {}", ex.getMessage());
                }
            }
        }, DEFAULT_REFRESH_TIMER, TimeUnit.MILLISECONDS);
    }
}
