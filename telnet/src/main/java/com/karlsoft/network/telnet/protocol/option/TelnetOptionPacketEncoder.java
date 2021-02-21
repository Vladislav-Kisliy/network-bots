package com.karlsoft.network.telnet.protocol.option;

import com.karlsoft.network.telnet.protocol.TelnetCommand;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.internal.StringUtil;

public class TelnetOptionPacketEncoder extends MessageToByteEncoder<TelnetOptionPacket> {

    public static final TelnetOptionPacketEncoder DEFAULT = new TelnetOptionPacketEncoder();

    @Override
    protected void encode(ChannelHandlerContext ctx,
                          TelnetOptionPacket msg, ByteBuf out) throws Exception {
        if (msg instanceof DefaultTelnetOptionPacket) {
            encodeOptionPacket((DefaultTelnetOptionPacket) msg, out);
        } else {
            throw new EncoderException("unsupported message type: " + StringUtil.simpleClassName(msg));
        }
    }

    private static void encodeOptionPacket(DefaultTelnetOptionPacket msg, ByteBuf out) {
        out.writeByte(TelnetCommand.IAC.getCode());
        out.writeByte(msg.getCommand().getCode());
        out.writeByte(msg.getOption().getCode());
    }
}
