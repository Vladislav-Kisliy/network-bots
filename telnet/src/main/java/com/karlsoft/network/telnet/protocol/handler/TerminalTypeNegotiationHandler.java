package com.karlsoft.network.telnet.protocol.handler;

import com.karlsoft.network.telnet.protocol.TelnetCommand;
import com.karlsoft.network.telnet.protocol.TelnetOption;
import com.karlsoft.network.telnet.protocol.packet.DefaultTelnetOptionPacket;
import com.karlsoft.network.telnet.protocol.packet.TelnetOptionPacket;
import com.karlsoft.network.telnet.protocol.setting.TerminalTypeSetting;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
public class TerminalTypeNegotiationHandler extends AbstractNegotiationHandler<TerminalTypeSetting> {

    public TerminalTypeNegotiationHandler(TerminalTypeSetting telnetSettings) {
        super(telnetSettings);
    }

    @Override
    public TelnetOptionPacket getResponse(TelnetOptionPacket in) {
        log.info("Terminal, got input packet ={}", in);
        TelnetOptionPacket response = null;
        switch (state) {
            case SENT_INITIAL:
                response = processAfterInitial(in);
                break;
            case SENT_SETTINGS:
                System.out.println("Got response");
                throw new IllegalStateException("Illegal state");

        }
        return response;
    }

    @Override
    public TelnetOptionPacket getInitialMessage() {
        checkpoint(State.SENT_INITIAL);
        TelnetOptionPacket response;
        if (telnetSettings.isInitialLocal()) {
            response = getOptionPacket(TelnetCommand.WILL);
        } else {
            response = getOptionPacket(TelnetCommand.DO);
        }
        return response;
    }

    private TelnetOptionPacket processAfterInitial(TelnetOptionPacket in) {
        TelnetCommand command = in.getCommand();
        System.out.println("processAfterInitial. command =" + command + ", option =" + Arrays.toString(in.getOption()));
        System.out.println("processAfterInitial options =" + telnetSettings);
        TelnetOptionPacket response = null;
        switch (command) {
            case WILL:
            case WONT:
            case DO: {
                if (telnetSettings.isInitialRemote()) {
                    response = getOptionPacket(command.positive());
//                    response = getTelnetOptions(command.positive());
                } else {
                    checkpoint(State.SENT_INITIAL);
                    response = getTelnetOptions(command.negative());
//                    response = getOptionPacket(command.negative());
                }
            }
            break;
            case DONT:
                break;
            default:
                throw new IllegalArgumentException("Unexpected command type =" + command);
        }
        return response;
    }

    private TelnetOptionPacket getOptionPacket(TelnetCommand command) {
        return DefaultTelnetOptionPacket.getOptionPacket(command, TelnetOption.TERMINAL_TYPE.getCode());
    }

    private TelnetOptionPacket getTelnetOptions(TelnetCommand command) {
        byte[] termType = telnetSettings.getTermType().getBytes(StandardCharsets.UTF_8);
        int[] options = new int[termType.length + 6];
        options[0] = command.getCode();
        options[1] = TelnetOption.TERMINAL_TYPE.getCode();
        options[2] = 0;
        byte offset = 3;
        for (int i = offset; i < termType.length + offset; i++) {
            options[i] = termType[i - offset];
        }
        options[termType.length + offset] = TelnetCommand.IAC.getCode();
        options[termType.length + offset + 1] = TelnetCommand.SE.getCode();

        return DefaultTelnetOptionPacket.getOptionPacket(command, options);
    }
}
