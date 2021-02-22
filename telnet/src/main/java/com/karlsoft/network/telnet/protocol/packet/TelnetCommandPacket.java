package com.karlsoft.network.telnet.protocol.packet;

import com.karlsoft.network.telnet.protocol.TelnetCommand;
import io.netty.handler.codec.DecoderResultProvider;

public interface TelnetCommandPacket extends DecoderResultProvider {
    TelnetCommand getCommand();
}
