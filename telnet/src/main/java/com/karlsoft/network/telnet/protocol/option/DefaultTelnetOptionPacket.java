package com.karlsoft.network.telnet.protocol.option;

import com.karlsoft.network.telnet.protocol.TelnetCommand;
import com.karlsoft.network.telnet.protocol.TelnetOption;
import io.netty.handler.codec.DecoderResult;
import io.netty.util.internal.ObjectUtil;
import lombok.Data;

@Data
public class DefaultTelnetOptionPacket extends DefaultTelnetCommandPacket implements TelnetOptionPacket {

    public static final TelnetOptionPacket ERROR = new DefaultTelnetOptionPacket(TelnetCommand.UNASSIGNED, TelnetOption.UNASSIGNED);
    private final TelnetOption option;

    public DefaultTelnetOptionPacket(TelnetCommand command, TelnetOption option) {
        super(command);
        this.option = option;
    }

    public static TelnetOptionPacket getOptionPacket(TelnetCommand command, TelnetOption option) {
        return new DefaultTelnetOptionPacket(command, option);
    }

}
