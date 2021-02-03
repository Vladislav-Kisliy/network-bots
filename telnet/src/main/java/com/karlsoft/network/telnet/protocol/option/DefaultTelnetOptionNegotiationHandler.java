package com.karlsoft.network.telnet.protocol.option;

/**
 * It ignores all incomming commands.
 */
public class DefaultTelnetOptionNegotiationHandler implements TelnetOptionNegotiationHandler {

    public static final TelnetOptionNegotiationHandler DEFAULT = new DefaultTelnetOptionNegotiationHandler();

    @Override
    public TelnetOptionPacket getResponse(TelnetOptionPacket in) {
        return new DefaultTelnetOptionPacket(in.getCommand().negative(), in.getOption());
    }
}
