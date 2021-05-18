package com.karlsoft.network.telnet.protocol.handler;

import com.karlsoft.network.telnet.protocol.TelnetOption;
import com.karlsoft.network.telnet.protocol.packet.TelnetCommandPacket;
import com.karlsoft.network.telnet.protocol.packet.TelnetOptionPacket;
import com.karlsoft.network.telnet.transport.HexUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumMap;

@Slf4j
public class OptionNegotiationHandler extends SimpleChannelInboundHandler<TelnetOptionPacket> {

    private final EnumMap<TelnetOption, TelnetOptionNegotiationHandler> negHandlers;

    public OptionNegotiationHandler(EnumMap<TelnetOption, TelnetOptionNegotiationHandler> negHandlers) {
        this.negHandlers = negHandlers;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TelnetOptionPacket packet) {
        if (packet instanceof TelnetCommandPacket) {
            if (packet.getCommand().isNegotiation()) {
                log.debug("Command {}, option {}", packet.getCommand(), TelnetOption.getOption(packet.getOption()[0]));
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
        TelnetOption option = TelnetOption.getOption(res.getOption());
        if (negHandlers.containsKey(option)) {
            handler = negHandlers.get(option);
            log.debug("Found handler {}, for command {}", handler, res.getCommand());
        } else {
            log.debug("Didn't find handler, for command {}. Will use default", res.getCommand());
            handler = DefaultTelnetOptionNegotiationHandler.DEFAULT;
        }
        return handler;
    }

}
