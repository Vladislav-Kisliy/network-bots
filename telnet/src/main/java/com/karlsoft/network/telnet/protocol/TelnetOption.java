package com.karlsoft.network.telnet.protocol;

import java.util.HashMap;
import java.util.Map;

public enum TelnetOption {
    BINARY(0),
    ECHO(1),
    RECONNECTION(2),
    SUPPRESS_GO_AHEAD(3),
    APPROXIMATE_MESSAGE_SIZE(4),
    STATUS(5),
    TIMING_MARK(6),
    REMOTE_CONTROLLED_TRANSMISSION(7),
    OUTPUT_LINE_WIDTH(8),
    OUTPUT_PAGE_SIZE(9),
    CARRIAGE_RETURN(10),
    HORIZONTAL_TAB_STOP(11),
    HORIZONTAL_TAB(12),
    FORMFEED(13),
    VERTICAL_TAB_STOP(14),
    VERTICAL_TAB(15),
    LINEFEED(16),
    EXTENDED_ASCII(17),
    FORCE_LOGOUT(18),
    BYTE_MACRO(19),
    DATA_ENTRY_TERMINAL(20),
    SUPDUP(21),
    SUPDUP_OUTPUT(22),
    SEND_LOCATION(23),
    TERMINAL_TYPE(24),
    END_OF_RECORD(25),
    TACACS_USER_IDENTIFICATION(26),
    OUTPUT_MARKING(27),
    TERMINAL_LOCATION_NUMBER(28),
    REGIME_3270(29),
    X3_PAD(30),
    WINDOW_SIZE(31),
    TERMINAL_SPEED(32),
    REMOTE_FLOW_CONTROL(33),
    LINEMODE(34),
    X_DISPLAY_LOCATION(35),
    OLD_ENVIRONMENT_VARIABLES(36),
    AUTHENTICATION(37),
    ENCRYPTION(38),
    NEW_ENVIRONMENT_VARIABLES(39),
    EXTENDED_OPTIONS_LIST(255),
    UNASSIGNED(-1);

    /*** The maximum value an option code can have.  This value is 255. ***/
    public static final int MAX_OPTION_VALUE = 255;


    private static final Map<Integer, TelnetOption> MAPPING = new HashMap<>();

    private final int code;

    TelnetOption(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    static {
        for (TelnetOption telnetOption : TelnetOption.values()) {
            MAPPING.put(telnetOption.code, telnetOption);
        }
    }

    /**
     * Returns the string representation of the telnet protocol option
     * corresponding to the given option code.
     * @param code The option code of the telnet protocol option
     * @return The representation of the telnet protocol option.
     */
    public static TelnetOption getOption(final int code) {
        TelnetOption result = TelnetOption.UNASSIGNED;
        if (MAPPING.containsKey(code)) {
            result = MAPPING.get(code);
        }

        return result;
    }


    /***
     * Determines if a given option code is valid.  Returns true if valid,
     * false if not.
     *
     * @param code  The option code to test.
     * @return True if the option code is valid, false if not.
     **/
    public static final boolean isValidOption(final int code) {
        return code >= BINARY.code && code <= EXTENDED_OPTIONS_LIST.code;
    }
}
