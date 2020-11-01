package com.karlsoft.network.telnet;

import com.karlsoft.network.telnet.client.TelnetClient;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Application {

    public static void main(String[] args) throws InterruptedException {
        // simulating a client to the relay server
//        final TcpClient client = TcpClient.create()
//                .host("localhost")
//                .port(connectedServer.address().getPort())
//                .secure(spec -> spec.sslContext(clientOptions));
//
//        Connection connectedClient = client
//                .doOnConnected(connection -> {
//                    connection.addHandlerLast("tlvEncoder", new TlvEncoder());
//                    connection.addHandlerLast("tlvDecoder", new TlvDecoder());
//                })
//                .doOnDisconnected(connection -> log.info("Client disconnected"))
//                .option(ChannelOption.SO_KEEPALIVE, true)
//


        final CountDownLatch latch = new CountDownLatch(1);

//        Connection client = TelnetClient.create()
//                .host("localhost")
//                .port(1823)
//                .handle((in, out) -> {
//                    in.receive()
//                            .log("conn")
//                            .subscribe(s -> latch.countDown());
//
//                    return out.sendString(Flux.just("Hello World!"))
//                            .neverComplete();
//                })
//                .wiretap(true)
//                .connectNow();

//        Connection connection =
//                TcpClient.create()
//                        .wiretap(true)
//                        .host("localhost")
//                        .port(1823)
//                        .handle((inbound, outbound) -> inbound.receive().then())
//                        .connectNow();
//
//        Connection connection =
//                TcpClient.create()
//                        .host("127.0.0.1")
//                        .port(1823)
//                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
////                        .remoteAddress(() -> new DomainSocketAddress("/tmp/test.sock"))
//                        .handle((inbound, outbound) -> inbound
//                                .receive()
//                                .asString()
//                                .flatMap(s -> {
//                                    System.out.println("In =" + s);
//                                    return outbound.sendString(Mono.just("hello friend!"));
//                                }))
//                        .wiretap(true)
//                        .connectNow();
//                        .handle((inbound, outbound) -> inbound.receive().asString() System.out.println(inbound.receive().asString())
//                        .handle((inbound, outbound) -> outbound.sendString(Mono.just("hello friend!")))

        Connection connection =
                TelnetClient
                        .create()
                        .autoDetectPrompt(true)
                        .host("127.0.0.1")
                        .port(1823)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
//                        .remoteAddress(() -> new DomainSocketAddress("/tmp/test.sock"))
                        .handle((inbound, outbound) -> inbound
                                .receive()
                                .asString()
                                .flatMap(s -> {
                                    System.out.println("In =" + s);
                                    return outbound.sendString(Mono.just("hello friend!"));
                                }))
                        .wiretap(true)
                        .connectNow();

        connection.onDispose().block();

        HttpClient client = HttpClient.create();
//
//        client
//                .compress(true)
//                .get()
//                .uri("http://example.com/")
//                .responseContent()
//                .aggregate()
//                .asString()
//                .block();
//
//        connection.onDispose()               .    block();

        System.out.println("Hello World!");
//        client.disposeNow();
    }

    private static void startServer() {
        System.out.println("Ready for start? Use netcat for listening to port");
        System.out.println("Small delay");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.error("InterruptedException: {}", e.getMessage());
        }
    }
}
