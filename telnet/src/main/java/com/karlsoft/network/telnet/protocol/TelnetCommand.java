package com.karlsoft.network.telnet.protocol;

import java.util.HashMap;
import java.util.Map;

public enum TelnetCommand {
    EOF(0xEC), SYNCH(0xF2), SUSP(0xED),
    ABORT(0xEE), EOR(0xEF), SE(0xF0),
    NOP(0xF1), DM(0xF2), BREAK(0xF3), IP(0xF4),
    AO(0xF5), AYT(0xF6), EC(0xF7), EL(0xF8), GA(0xF9),
    SB(0xFA) {
        @Override
        public boolean isNegotiation() {
            return true;
        }
    },
    WILL(0xFB) {
        @Override
        public boolean isNegotiation() {
            return true;
        }

        @Override
        public TelnetCommand positive() {
            return DO;
        }

        @Override
        public TelnetCommand negative() {
            return DONT;
        }
    },
    WONT(0xFC) {
        @Override
        public boolean isNegotiation() {
            return true;
        }

        @Override
        public TelnetCommand positive() {
            return DONT;
        }

        @Override
        public TelnetCommand negative() {
            return DO;
        }
    },
    DO(0xFD) {
        @Override
        public boolean isNegotiation() {
            return true;
        }

        @Override
        public TelnetCommand positive() {
            return WILL;
        }

        @Override
        public TelnetCommand negative() {
            return WONT;
        }
    },
    DONT(0xFE) {
        @Override
        public boolean isNegotiation() {
            return true;
        }

        @Override
        public TelnetCommand positive() {
            return WONT;
        }

        @Override
        public TelnetCommand negative() {
            return WILL;
        }
    },
    IAC(0xFF),
    UNASSIGNED(-1);

    private static final TelnetCommand FIRST_COMMAND = IAC;
    private static final TelnetCommand LAST_COMMAND = EOF;
    private static final Map<Integer, TelnetCommand> MAPPING = new HashMap<>();

    private int code;

    TelnetCommand(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public TelnetCommand positive() {
        return UNASSIGNED;
    }

    public TelnetCommand negative() {
        return UNASSIGNED;
    }

    public boolean isNegotiation() {
        return false;
    }

    static {
        for (TelnetCommand telnetCommand : TelnetCommand.values()) {
            MAPPING.put(telnetCommand.code, telnetCommand);
        }
    }

    public static TelnetCommand getCommand(final int code) {
        TelnetCommand result = TelnetCommand.UNASSIGNED;
        if (MAPPING.containsKey(code)) {
            result = MAPPING.get(code);
        }

        return result;
    }

    /**
     * Determines if a given command code is valid.  Returns true if valid, false if not.
     * <p>
     *
     * @param code The command code to test.
     * @return True if the command code is valid, false if not.
     */
    public static boolean isValidCommand(final int code) {
        return code <= FIRST_COMMAND.code && code >= LAST_COMMAND.code;
    }
}
