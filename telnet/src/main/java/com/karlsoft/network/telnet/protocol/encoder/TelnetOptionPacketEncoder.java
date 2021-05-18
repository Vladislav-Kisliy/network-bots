package com.karlsoft.network.telnet.protocol.encoder;

import com.karlsoft.network.telnet.protocol.TelnetCommand;
import com.karlsoft.network.telnet.protocol.packet.DefaultTelnetOptionPacket;
import com.karlsoft.network.telnet.protocol.packet.TelnetOptionPacket;
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
            encodeOptionPacket(msg, out);
        } else {
            throw new EncoderException("Unsupported message type: " + StringUtil.simpleClassName(msg));
        }
    }

    private static void encodeOptionPacket(TelnetOptionPacket msg, ByteBuf out) {
        if (msg != null) {
            out.writeByte(TelnetCommand.IAC.getCode());
            out.writeByte(msg.getCommand().getCode());
            for (int i: msg.getOption()) {
                out.writeByte(i);
            }
        }
    }
}
