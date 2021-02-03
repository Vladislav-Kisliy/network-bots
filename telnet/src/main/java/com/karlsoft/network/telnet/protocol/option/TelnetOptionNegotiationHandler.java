package com.karlsoft.network.telnet.protocol.option;

import io.netty.buffer.ByteBuf;

@FunctionalInterface
public interface TelnetOptionNegotiationHandler {
    TelnetOptionPacket getResponse(TelnetOptionPacket in);
}
