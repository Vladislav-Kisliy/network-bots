package com.karlsoft.network.telnet.client;

import com.karlsoft.network.telnet.client.auth.Credentials;
import com.karlsoft.network.telnet.client.auth.UsernamePasswordCredentials;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpResources;
import reactor.netty.transport.ClientTransport;

import java.time.Duration;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static reactor.netty.ReactorNetty.format;

@Slf4j
public abstract class TelnetClient extends ClientTransport<TelnetClient, TelnetClientConfig> {

    public static TelnetClient create() {
        return create(TcpResources.get());
    }

    public static TelnetClient create(ConnectionProvider provider) {
        requireNonNull(provider, "provider");
        return new TelnetClientConnect(provider);
    }

    public TelnetClient handle(BiFunction<? super NettyInbound, ? super NettyOutbound, ? extends Publisher<Void>> handler) {
        Objects.requireNonNull(handler, "handler");
        return doOnConnected(new TelnetClient.OnConnectedHandle(handler));
    }

    @Override
    public Mono<? extends Connection> connect() {
        return super.connect();
    }

    @Override
    public final Connection connectNow() {
        return super.connectNow();
    }

    @Override
    public final Connection connectNow(Duration timeout) {
        return super.connectNow(timeout);
    }

    public final TelnetClient autoDetectPrompt(boolean compressionEnabled) {
        return this;
    }

    public final TelnetClient creds(@NonNull String userName, @NonNull String password) {
        Credentials credentials = new UsernamePasswordCredentials(userName, password.toCharArray());
        TelnetClient dup = duplicate();
        dup.configuration().credentials = credentials;
        return dup;
    }
//    public final TelnetClient compress(boolean compressionEnabled) {
//        if (compressionEnabled) {
//            if (!configuration().acceptGzip) {
////                HttpClient dup = duplicate();
////                HttpHeaders headers = configuration().headers.copy();
////                headers.add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
////                dup.configuration().headers = headers;
////                dup.configuration().acceptGzip = true;
////                return dup;
//            }
//        }
//        else if (configuration().acceptGzip) {
//            HttpClient dup = duplicate();
//            if (isCompressing(configuration().headers)) {
//                HttpHeaders headers = configuration().headers.copy();
//                headers.remove(HttpHeaderNames.ACCEPT_ENCODING);
//                dup.configuration().headers = headers;
//            }
//            dup.configuration().acceptGzip = false;
//            return dup;
//        }
//        return this;
//    }

    static final class OnConnectedHandle implements Consumer<Connection> {

        final BiFunction<? super NettyInbound, ? super NettyOutbound, ? extends Publisher<Void>> handler;

        OnConnectedHandle(BiFunction<? super NettyInbound, ? super NettyOutbound, ? extends Publisher<Void>> handler) {
            this.handler = handler;
        }

        @Override
        public void accept(Connection c) {
            if (log.isDebugEnabled()) {
                log.debug(format(c.channel(), "Handler is being applied: {}"), handler);
            }
            Mono.fromDirect(handler.apply((NettyInbound) c, (NettyOutbound) c))
                    .subscribe(c.disposeSubscriber());
        }
    }
}
