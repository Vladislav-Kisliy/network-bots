package com.karlsoft.network.telnet.protocol.packet;

public interface TelnetOptionPacket extends TelnetCommandPacket {
    int[] getOption();
}
