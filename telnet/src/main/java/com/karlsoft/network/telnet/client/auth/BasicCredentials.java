package com.karlsoft.network.telnet.client.auth;

import lombok.Data;

@Data
public class BasicCredentials implements Credentials {
    private final String userName;
    private final char[] password;
}
