package com.karlsoft.network.telnet.protocol.handler;

import com.karlsoft.network.telnet.protocol.packet.TelnetOptionPacket;
import com.karlsoft.network.telnet.protocol.setting.TelnetSetting;

public abstract class AbstractNegotiationHandler<T extends TelnetSetting> implements TelnetOptionNegotiationHandler {

    protected enum State {
        INIT,
        SENT_INITIAL,
        SENT_SETTINGS,
        RECIEVED_RESPONSE,
        FAILURE
    }

    protected final T telnetSettings;
    protected State state = State.INIT;

    public AbstractNegotiationHandler(T telnetSettings) {
        this.telnetSettings = telnetSettings;
    }

    @Override
    public TelnetOptionPacket getInitialMessage() {
        return null;
    }

    public T getTelnetSettings() {
        return telnetSettings;
    }

    protected void checkpoint(State state) {
        this.state = state;
    }
}
