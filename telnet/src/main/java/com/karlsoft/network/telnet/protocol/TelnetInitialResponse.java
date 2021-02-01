package com.karlsoft.network.telnet.protocol;

public interface TelnetInitialResponse {
    TelnetCommand getCommand();

    TelnetOption getOption();
}
