package com.karlsoft.network.telnet.protocol.handler;

import com.karlsoft.network.telnet.protocol.packet.DefaultTelnetOptionPacket;
import com.karlsoft.network.telnet.protocol.packet.TelnetOptionPacket;

/**
 * It ignores all incoming commands.
 */
public class DefaultTelnetOptionNegotiationHandler implements TelnetOptionNegotiationHandler {

    public static final TelnetOptionNegotiationHandler DEFAULT = new DefaultTelnetOptionNegotiationHandler();

    @Override
    public TelnetOptionPacket getResponse(TelnetOptionPacket in) {
        return new DefaultTelnetOptionPacket(in.getCommand().negative(), in.getOption());
    }

    @Override
    public TelnetOptionPacket getInitialMessage() {
        return null;
    }
}
