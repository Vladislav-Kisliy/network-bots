package com.karlsoft.network.telnet.protocol.handler;

import com.karlsoft.network.telnet.client.auth.Credentials;
import com.karlsoft.network.telnet.transport.HexUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.ConnectException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginHandler extends SimpleChannelInboundHandler<String> {

    enum State {
        LOGIN,
        PASSWORD,
        PASSWORD_SENT,
        SUCCESS,
        FAILURE;
    }

    public static final String CRLF = "\r\n";
    public static final String INCORRECT_LOGIN_MESSAGE = "incorrect";
    public static final String INCORRECT_LOGIN_MESSAGE2 = "Error in authentication";
    public static final String DEFAULT_PROMPT_MESSAGE = ">|>\\s|#|#\\s";
    private static final String LOGIN_MESSAGE = "sername:|ogin:";
    private static final String PASSWORD_MESSAGE = "assword:|PASSCODE:";
    private static final String INCORRECT_LOGIN_CISCO = "Username";

    private State state;

    private final Credentials creds;
    private final StringBuilder builder;

    public LoginHandler(Credentials creds) {
        this.creds = creds;
        builder = new StringBuilder();
        state = State.LOGIN;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        builder.append(s);
        System.out.println("builder =" + builder);
        HexUtils.debugOutput(builder);
        switch (state()) {
            case LOGIN: {
                Matcher matcher = Pattern.compile(LOGIN_MESSAGE).matcher(builder);
                if (matcher.find()) {
                    ctx.channel().writeAndFlush(creds.getUserName() + CRLF);
                    builder.setLength(0);
                    checkpoint(State.PASSWORD);
                }
            }
            break;
            case PASSWORD: {
                Matcher matcher = Pattern.compile(PASSWORD_MESSAGE).matcher(builder);
                if (matcher.find()) {
                    String password = new String(creds.getPassword()) + CRLF;
                    ctx.channel().writeAndFlush(password);
                    builder.setLength(0);
                    checkpoint(State.PASSWORD_SENT);
                }
            }
            break;
            case PASSWORD_SENT: {
                if (builder.indexOf(INCORRECT_LOGIN_MESSAGE) >= 0 || builder.indexOf(INCORRECT_LOGIN_MESSAGE2) >= 0 ||
                        builder.indexOf(INCORRECT_LOGIN_CISCO) >= 0) {
                    checkpoint(State.FAILURE);
                    throw new ConnectException("Incorrect login/pw. Login: " + creds.getUserName());
                }
                Matcher matcher = Pattern.compile(DEFAULT_PROMPT_MESSAGE).matcher(builder);
                if (matcher.find()) {
                    ctx.channel().writeAndFlush(CRLF);
                    builder.setLength(0);
                    checkpoint(State.SUCCESS);
                }
            }
            break;
            case SUCCESS: {
                System.out.println("Prompt =" + builder);
            }
            break;
        }
    }

    private State state() {
        return state;
    }

    private void checkpoint(State state) {
        this.state = state;
    }
}
