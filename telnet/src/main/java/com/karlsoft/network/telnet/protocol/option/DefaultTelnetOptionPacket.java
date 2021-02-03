package com.karlsoft.network.telnet.protocol.option;

import com.karlsoft.network.telnet.protocol.TelnetCommand;
import com.karlsoft.network.telnet.protocol.TelnetOption;
import io.netty.handler.codec.DecoderResult;
import io.netty.util.internal.ObjectUtil;
import lombok.Data;

@Data
public class DefaultTelnetOptionPacket implements TelnetOptionPacket {

    public static final TelnetOptionPacket ERROR = new DefaultTelnetOptionPacket(TelnetCommand.UNASSIGNED, TelnetOption.UNASSIGNED);
    private final TelnetCommand command;
    private final TelnetOption option;
    private DecoderResult decoderResult = DecoderResult.SUCCESS;

    public static TelnetOptionPacket getInitialResponse(TelnetCommand command, TelnetOption option) {
        return new DefaultTelnetOptionPacket(command, option);
    }

    @Override
    public DecoderResult decoderResult() {
        return decoderResult;
    }

    @Override
    public void setDecoderResult(DecoderResult decoderResult) {
        this.decoderResult = ObjectUtil.checkNotNull(decoderResult, "decoderResult");
    }
}
