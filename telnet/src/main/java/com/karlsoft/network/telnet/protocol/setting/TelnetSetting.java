package com.karlsoft.network.telnet.protocol.setting;

import com.karlsoft.network.telnet.protocol.TelnetOption;

public interface TelnetSetting {
    TelnetOption getTelnetOption();

    boolean isInitialLocal();

    boolean isInitialRemote();

    boolean isAcceptLocal();

    boolean isAcceptRemote();
}
