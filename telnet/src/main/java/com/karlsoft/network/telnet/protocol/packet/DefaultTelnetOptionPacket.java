package com.karlsoft.network.telnet.protocol.packet;

import com.karlsoft.network.telnet.protocol.TelnetCommand;
import com.karlsoft.network.telnet.protocol.TelnetOption;

import java.util.Arrays;

public class DefaultTelnetOptionPacket extends DefaultTelnetCommandPacket implements TelnetOptionPacket {

    public static final TelnetOptionPacket ERROR = new DefaultTelnetOptionPacket(TelnetCommand.UNASSIGNED,
            TelnetOption.UNASSIGNED.getCode());

    private final int[] options;

    public DefaultTelnetOptionPacket(TelnetCommand command, int[] options) {
        super(command);
        this.options = options;
    }

    public DefaultTelnetOptionPacket(TelnetCommand command, int option) {
        this(command, new int[]{option});
    }

    @Override
    public int[] getOption() {
        return options;
    }

    @Override
    public String toString() {
        return "DefaultTelnetOptionPacket{" +
                "options=" + Arrays.toString(options) +
                '}';
    }

    public static TelnetOptionPacket getOptionPacket(TelnetCommand command, int[] options) {
        return new DefaultTelnetOptionPacket(command, options);
    }

    public static TelnetOptionPacket getOptionPacket(TelnetCommand command, int option) {
        return new DefaultTelnetOptionPacket(command, option);
    }


}
