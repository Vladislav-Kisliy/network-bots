package com.karlsoft.network.telnet.protocol.handler;

import com.karlsoft.network.telnet.protocol.packet.TelnetOptionPacket;

public interface TelnetOptionNegotiationHandler {
    TelnetOptionPacket getResponse(TelnetOptionPacket in);

    TelnetOptionPacket getInitialMessage();
}
