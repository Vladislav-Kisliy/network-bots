/*
 * Copyright (c) 2011-Present VMware, Inc. or its affiliates, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.karlsoft.network.telnet.client;

import io.netty.channel.ChannelOption;
import io.netty.util.NetUtil;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.transport.AddressUtils;

import java.util.Collections;

class TelnetClientConnect extends TelnetClient {

    private static final int DEFAULT_PORT = 23;


    private final TelnetClientConfig config;

    TelnetClientConnect(ConnectionProvider provider) {
        this.config = new TelnetClientConfig(provider, Collections.singletonMap(ChannelOption.AUTO_READ, false),
                () -> AddressUtils.createUnresolved(NetUtil.LOCALHOST.getHostAddress(), DEFAULT_PORT));
    }

    TelnetClientConnect(TelnetClientConfig config) {
        this.config = config;
    }

    @Override
    public TelnetClientConfig configuration() {
        return config;
    }

    @Override
    protected TelnetClient duplicate() {
        return new TelnetClientConnect(new TelnetClientConfig(config));
    }

}
