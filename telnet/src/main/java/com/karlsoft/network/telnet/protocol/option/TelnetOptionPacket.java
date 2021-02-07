package com.karlsoft.network.telnet.protocol.option;

import com.karlsoft.network.telnet.protocol.TelnetCommand;
import com.karlsoft.network.telnet.protocol.TelnetOption;
import io.netty.handler.codec.DecoderResultProvider;

public interface TelnetOptionPacket extends TelnetCommandPacket {
    TelnetOption getOption();
}
