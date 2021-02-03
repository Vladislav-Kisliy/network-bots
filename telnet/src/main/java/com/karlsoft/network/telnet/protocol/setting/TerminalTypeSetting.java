package com.karlsoft.network.telnet.protocol.setting;

import com.karlsoft.network.telnet.protocol.TelnetOption;

public class TerminalTypeSetting extends AbstractTelnetSetting {
    private final String termType;

    public TerminalTypeSetting(String termType, boolean initialLocal, boolean initialRemote, boolean acceptLocal, boolean acceptRemote) {
        super(TelnetOption.TERMINAL_TYPE, initialLocal, initialRemote, acceptLocal, acceptRemote);
        this.termType = termType;
    }

    public String getTermType() {
        return termType;
    }
}
