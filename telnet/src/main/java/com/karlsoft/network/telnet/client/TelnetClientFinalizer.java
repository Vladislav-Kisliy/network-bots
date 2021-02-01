package com.karlsoft.network.telnet.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelOption;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.channel.ChannelOperations;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

final class TelnetClientFinalizer extends TelnetClientConnect implements TelnetClient.RequestSender {

    public TelnetClientFinalizer(TelnetClientConfig config) {
        super(config);
    }

    @Override
    public ResponseReceiver execute(String command) {
        Objects.requireNonNull(command, "command");
//        TelnetClient dup = duplicate();
        TelnetClientFinalizer dup = new TelnetClientFinalizer(new TelnetClientConfig(configuration()));
        dup.configuration().command = command;
        System.out.println("f =" + dup.getClass().getName());
        dup.configuration().command = command;
//        return (TelnetClientFinalizer) dup;
        return dup;
    }

    @Override
    public ResponseReceiver execute(Mono<String> command) {
        Objects.requireNonNull(command, "command");
        TelnetClientFinalizer dup = new TelnetClientFinalizer(new TelnetClientConfig(configuration()));
        dup.configuration().deferredConf(config -> command.map(s -> {
            config.command = s;
            return config;
        }));
        return dup;
    }

    @Override
    public ByteBufFlux responseContent() {
        ByteBufAllocator alloc = (ByteBufAllocator) configuration().options()
                .get(ChannelOption.ALLOCATOR);
        if (alloc == null) {
            alloc = ByteBufAllocator.DEFAULT;
        }

        @SuppressWarnings("unchecked")
        Mono<ChannelOperations<?, ?>> connector = (Mono<ChannelOperations<?, ?>>) connect();
        return ByteBufFlux.fromInbound(connector.flatMapMany(contentReceiver), alloc);
    }

    @Override
    public Mono responseSingle(BiFunction receiver) {
        return null;
    }

    @Override
    public Flux responseConnection(BiFunction receiver) {
        return null;
    }

    @Override
    public Flux response(BiFunction receiver) {
        return null;
    }

    static final Function<ChannelOperations<?, ?>, Publisher<ByteBuf>> contentReceiver = ChannelOperations::receive;

//    static final Function<HttpClientOperations, HttpClientResponse> RESPONSE_ONLY = ops -> {
//        //defer the dispose to avoid over disposing on receive
//        discard(ops);
//        return ops;
//    };
}
