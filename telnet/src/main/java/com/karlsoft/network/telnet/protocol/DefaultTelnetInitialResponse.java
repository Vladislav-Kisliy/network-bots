package com.karlsoft.network.telnet.protocol;

import lombok.Data;

@Data
public class DefaultTelnetInitialResponse implements TelnetInitialResponse {
    private final TelnetCommand command;
    private final TelnetOption option;

    public static TelnetInitialResponse getInitialResponse(TelnetCommand command, TelnetOption option) {
        return new DefaultTelnetInitialResponse(command, option);
    }
}
