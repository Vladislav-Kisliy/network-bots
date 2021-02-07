package com.karlsoft.network.telnet.client;

import com.karlsoft.network.telnet.client.auth.Credentials;
import com.karlsoft.network.telnet.protocol.setting.TelnetSetting;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.ByteBufMono;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpResources;
import reactor.netty.transport.ClientTransport;

import java.time.Duration;
import java.util.List;
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

    public final TelnetClient autoDetectPrompt(boolean promptDetectionEnabled) {
        TelnetClient dup = duplicate();
        dup.configuration().promptDetectionEnabled = promptDetectionEnabled;
        return dup;
    }

    public final TelnetClient telnetSettings(List<TelnetSetting> telnetSettings) {
        TelnetClient dup = duplicate();
        dup.configuration().telnetSettings = telnetSettings;
        return dup;
    }

    public final TelnetClient telnetSessionTimeout(long telnetSessionTimeout) {
        TelnetClient dup = duplicate();
        dup.configuration().telnetSessionTimeout = telnetSessionTimeout;
        return dup;
    }

    public final RequestSender creds(@NonNull Credentials credentials) {
        Objects.requireNonNull(credentials, "credentials");
        TelnetClientFinalizer dup = new TelnetClientFinalizer(new TelnetClientConfig(configuration()));
        dup.configuration().credentials = credentials;
        System.out.println("d =" + dup.getClass().getName());
        return dup;
    }

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

    public interface RequestSender extends ResponseReceiver {

        ResponseReceiver execute(String command);

        ResponseReceiver execute(Mono<String> command);

    }

    public interface ResponseReceiver<S extends ResponseReceiver<?>> {

        <V> Flux<V> response(BiFunction<? super TelnetClientResponse, ? super ByteBufFlux, ? extends Publisher<V>> receiver);

        <V> Flux<V> responseConnection(BiFunction<? super TelnetClientResponse, ? super Connection, ? extends Publisher<V>> receiver);

        ByteBufFlux responseContent();

        <V> Mono<V> responseSingle(BiFunction<? super TelnetClientResponse, ? super ByteBufMono, ? extends Mono<V>> receiver);
    }
}
