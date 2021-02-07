package com.karlsoft.network.telnet.protocol.option;

import com.karlsoft.network.telnet.protocol.TelnetCommand;
import com.karlsoft.network.telnet.protocol.TelnetOption;

public interface TelnetOptionSubnegotiationPacket extends TelnetOptionPacket {
    int[] getOptions();

    TelnetCommand getLastCommand();

    TelnetOption getLastOption();
}
