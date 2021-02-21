package com.karlsoft.network.telnet.protocol.newpack;

import com.karlsoft.network.telnet.protocol.TelnetOption;
import com.karlsoft.network.telnet.protocol.option.DefaultTelnetOptionNegotiationHandler;
import com.karlsoft.network.telnet.protocol.option.TelnetCommandPacket;
import com.karlsoft.network.telnet.protocol.option.TelnetOptionNegotiationHandler;
import com.karlsoft.network.telnet.protocol.option.TelnetOptionPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumMap;

@Slf4j
public class OptionNegotiationHandler extends SimpleChannelInboundHandler<TelnetOptionPacket> {

    private final EnumMap<TelnetOption, TelnetOptionNegotiationHandler> negHandlers;

    public OptionNegotiationHandler() {
        this.negHandlers = new EnumMap<>(TelnetOption.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TelnetOptionPacket packet) throws Exception {
        if (packet instanceof TelnetCommandPacket) {
            if (packet.getCommand().isNegotiation()) {
                log.debug("Command {}, option {}", packet.getCommand(), packet.getOption());
                TelnetOptionNegotiationHandler handler = getNegotiationHandler(packet);
                ctx.channel().writeAndFlush(handler.getResponse(packet));
            } else {
                handleCommand(packet);
            }
        } else {
            String message = "unsupported frame type: " + packet.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }

    private void handleCommand(TelnetCommandPacket res) {
        log.debug("Command {}", res.getCommand());
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

}
