package com.karlsoft.network.telnet.client.auth;

import com.karlsoft.network.telnet.client.auth.Credentials;
import lombok.Data;

@Data
public class UsernamePasswordCredentials implements Credentials {
    private final String userName;
    private final char[] password;
}
