package com.karlsoft.network.telnet.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class TelnetInitialResponseDecoder extends ReplayingDecoder<TelnetInitialResponseDecoder.State> {

    enum State {
        INIT,
        SUCCESS,
        FAILURE
    }

    public TelnetInitialResponseDecoder() {
        super(State.INIT);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        System.out.println("decode");
        try {
            switch (state()) {
                case INIT: {
                    final short readByte = in.readUnsignedByte();
                    System.out.println("Found telnet command =" + TelnetCommand.getCommand(readByte));
//                    if (readByte != SocksVersion.SOCKS5.byteValue()) {
                    if (TelnetCommand.getCommand(readByte) == TelnetCommand.IAC) {
                        log.debug("Found telnet command");
                        TelnetCommand command = TelnetCommand.getCommand(in.readUnsignedByte());
                        TelnetOption option = TelnetOption.getOption(in.readUnsignedByte());
//                        final Socks5AuthMethod authMethod = Socks5AuthMethod.valueOf(in.readByte());
                        out.add(DefaultTelnetInitialResponse.getInitialResponse(command, option));
                        checkpoint(State.SUCCESS);
                    } else {
                        log.warn("Couldn't find telnet commands. Is it telnet server?");
                        in.skipBytes(actualReadableBytes());
                    }
                    break;
                }
                case SUCCESS: {
                    int readableBytes = actualReadableBytes();
                    if (readableBytes > 0) {
                        out.add(in.readRetainedSlice(readableBytes));
                    }
                    break;
                }
                case FAILURE: {
                    in.skipBytes(actualReadableBytes());
                    break;
                }
            }
        } catch (Exception e) {
            fail(out, e);
        }
    }

    private void fail(List<Object> out, Exception cause) {
//        if (!(cause instanceof DecoderException)) {
//            cause = new DecoderException(cause);
//        }

        checkpoint(State.FAILURE);

//        Socks5Message m = new DefaultSocks5InitialResponse(Socks5AuthMethod.UNACCEPTED);
//        m.setDecoderResult(DecoderResult.failure(cause));
//        out.add(m);
    }
}