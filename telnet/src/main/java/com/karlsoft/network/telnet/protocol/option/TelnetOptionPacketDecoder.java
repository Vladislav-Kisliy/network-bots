package com.karlsoft.network.telnet.protocol.option;

import com.karlsoft.network.telnet.protocol.TelnetCommand;
import com.karlsoft.network.telnet.protocol.TelnetOption;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class TelnetOptionPacketDecoder extends ReplayingDecoder<TelnetOptionPacketDecoder.State> {

    enum State {
        WAITING_MARK,
        READ_COMMAND,
        READ_OPTION,
        FAILURE
    }

    private TelnetCommand command;

    public TelnetOptionPacketDecoder() {
        super(State.WAITING_MARK);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        try {
            switch (state()) {
                case WAITING_MARK: {
                    final short readByte = buf.readUnsignedByte();
                    if (TelnetCommand.getCommand(readByte) == TelnetCommand.IAC) {
                        log.debug("Found telnet mark");
                        checkpoint(State.READ_COMMAND);
                    } else {
                        log.debug("Skip other characters");
                        int readableBytes = actualReadableBytes();
                        if (readableBytes > 0) {
                            out.add(buf.readRetainedSlice(readableBytes));
                        }
                    }
                    break;
                }
                case READ_COMMAND: {
                    command = TelnetCommand.getCommand(buf.readUnsignedByte());
                    checkpoint(State.READ_OPTION);
                    break;
                }
                case READ_OPTION: {
                    TelnetOption option = TelnetOption.getOption(buf.readUnsignedByte());
                    out.add(DefaultTelnetOptionPacket.getInitialResponse(command, option));
                    checkpoint(State.WAITING_MARK);
                    break;
                }
                default:
                    throw new RuntimeException("Shouldn't reach here.");
            }
        } catch (Exception e) {
            fail(out, e);
        }
    }

    private void fail(List<Object> out, Exception cause) {
        if (!(cause instanceof DecoderException)) {
            cause = new DecoderException(cause);
        }

        checkpoint(State.FAILURE);

        TelnetOptionPacket m = DefaultTelnetOptionPacket.ERROR;
        m.setDecoderResult(DecoderResult.failure(cause));
        out.add(m);
    }
}