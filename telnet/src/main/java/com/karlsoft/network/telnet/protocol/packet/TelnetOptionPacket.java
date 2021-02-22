package com.karlsoft.network.telnet.protocol.packet;

import com.karlsoft.network.telnet.protocol.TelnetOption;

public interface TelnetOptionPacket extends TelnetCommandPacket {
    TelnetOption getOption();
}
