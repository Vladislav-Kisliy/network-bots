package com.karlsoft.network.telnet.protocol.packet;

import com.karlsoft.network.telnet.protocol.TelnetCommand;
import io.netty.handler.codec.DecoderResult;
import io.netty.util.internal.ObjectUtil;
import lombok.Data;

@Data
public class DefaultTelnetCommandPacket implements TelnetCommandPacket {

    public static final TelnetCommandPacket ERROR = new DefaultTelnetCommandPacket(TelnetCommand.UNASSIGNED);
    private final TelnetCommand command;
    private DecoderResult decoderResult = DecoderResult.SUCCESS;

    @Override
    public DecoderResult decoderResult() {
        return decoderResult;
    }

    @Override
    public void setDecoderResult(DecoderResult decoderResult) {
        this.decoderResult = ObjectUtil.checkNotNull(decoderResult, "decoderResult");
    }

    public static TelnetCommandPacket getCommandPacket(TelnetCommand command) {
        return new DefaultTelnetCommandPacket(command);
    }
}
