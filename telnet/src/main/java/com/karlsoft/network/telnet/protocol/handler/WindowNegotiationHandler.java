package com.karlsoft.network.telnet.protocol.handler;

import com.karlsoft.network.telnet.protocol.packet.TelnetOptionPacket;
import com.karlsoft.network.telnet.protocol.setting.AbstractTelnetSetting;
import com.karlsoft.network.telnet.protocol.setting.TelnetSetting;
import com.karlsoft.network.telnet.protocol.setting.WindowSizeSetting;

public class WindowNegotiationHandler implements TelnetOptionNegotiationHandler {

    private final WindowSizeSetting windowSetting;

    public WindowNegotiationHandler(TelnetSetting windowSetting) {
        this.windowSetting = (WindowSizeSetting) windowSetting;
    }

    @Override
    public TelnetOptionPacket getResponse(TelnetOptionPacket in) {
        return null;
    }

    @Override
    public TelnetOptionPacket getInitialMessage() {
        return null;
    }
}
