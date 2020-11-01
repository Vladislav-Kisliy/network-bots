package com.karlsoft.network.telnet.client;

import reactor.netty.ConnectionObserver;

public enum  TelnetClientState implements ConnectionObserver.State {

    AUTHORIZED() {
        @Override
        public String toString() {
            return "[authorized]";
        }
    },
    NOT_AUTHORIZED() {
        @Override
        public String toString() {
            return "[not_authorized]";
        }
    }
}
