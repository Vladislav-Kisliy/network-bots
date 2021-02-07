package com.karlsoft.network.telnet.transport;

import java.nio.charset.StandardCharsets;

public class HexUtils {

    public static void debugOutput(String line) {
        StringBuilder sb = new StringBuilder();
        for (byte b : line.getBytes(StandardCharsets.UTF_8)) {
            sb.append(String.format("%02X ", b));
        }
        System.out.println("hexString [" + sb.toString() + "]");
    }

    public static void debugOutput(StringBuilder builder) {
        debugOutput(builder.toString());
    }
}
