package com.karlsoft.network.telnet.protocol.setting;

import com.karlsoft.network.telnet.protocol.TelnetOption;
import lombok.Data;

@Data
public abstract class AbstractTelnetSetting implements TelnetSetting {

    private final TelnetOption telnetOption;
    private final boolean initialLocal;
    private final boolean initialRemote;
    private final boolean acceptLocal;
    private final boolean acceptRemote;

    public AbstractTelnetSetting(TelnetOption telnetOption) {
        this(telnetOption, false, false, false, false);
    }

    public AbstractTelnetSetting(TelnetOption telnetOption, boolean initialLocal, boolean initialRemote, boolean acceptLocal, boolean acceptRemote) {
        this.telnetOption = telnetOption;
        this.initialLocal = initialLocal;
        this.initialRemote = initialRemote;
        this.acceptLocal = acceptLocal;
        this.acceptRemote = acceptRemote;
    }
}
