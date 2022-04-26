package cn.fantasticmao.grpckit.proto;

import cn.fantasticmao.grpckit.boot.GrpcKitChannelBuilder;
import cn.fantasticmao.grpckit.boot.GrpcKitConfig;
import cn.fantasticmao.grpckit.boot.GrpcKitServerBuilder;
import cn.fantasticmao.grpckit.boot.GrpcKitStubFactory;
import io.grpc.Channel;
import io.grpc.Server;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * GreeterServiceTest
 * <p>
 * <h3>Start a ZooKeeper container for testing method {@link GreeterServiceTest#example1()}:</h3>
 *
 * {@code docker run -d -p 2181:2181 --rm --name zookeeper-test zookeeper:3.7.0}
 * <p>
 * <h3>Start a nacos container for testing method {@link GreeterServiceTest#example2()}:</h3>
 * {@code docker run --name nacos -e MODE=standalone -p 8848:8848 -p 9848:9848 -p 9849:9849 -d nacos/nacos-server:2.0.2}
 * <p>
 * @author fantasticmao
 * @author harrison
 * @version 1.39.0
 * @since 2021-07-31
 */
@Disabled
public class GreeterServiceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(GreeterServiceTest.class);

    /**
     * 使用zk作为注册中心，且负载均衡策略为Random
     * @throws IOException
     */
    @Test
    public void example1() throws IOException {
        final String appName = "unit_test";

        // build servers
        final GrpcKitConfig serverConfig_1 = GrpcKitConfig.loadAndParse("grpc-kit-server-1.yml");
        final GrpcKitConfig serverConfig_2 = GrpcKitConfig.loadAndParse("grpc-kit-server-2.yml");

        final Server server_1 = GrpcKitServerBuilder.forConfig(appName, serverConfig_1)
            .addService(new GreeterService(1))
            .build();
        final Server server_2 = GrpcKitServerBuilder.forConfig(appName, serverConfig_2)
            .addService(new GreeterService(2))
            .build();

        // start servers
        server_1.start();
        server_2.start();

        try {
            // new channel and stub
            final GrpcKitConfig clientConfig = GrpcKitConfig.loadAndParse("grpc-kit-client-1.yml");
            final Channel channel = GrpcKitChannelBuilder.forConfig(appName, clientConfig.validate())
                .usePlaintext()
                .build();
            final GreeterServiceGrpc.GreeterServiceBlockingStub stub = GrpcKitStubFactory.newStub(
                GreeterServiceGrpc.GreeterServiceBlockingStub.class, channel, clientConfig.validate());

            // send requests
            HelloRequest request = HelloRequest.newBuilder()
                .setName("fantasticmao")
                .build();
            for (int i = 0; i < 10; i++) {
                LOGGER.info("[Stub] greeting, name: {}", request.getName());
                HelloResponse response = stub.sayHello(request);
                LOGGER.info("[Stub] receive a new message: {}", response.getMessage());
            }
        } finally {
            // shutdown servers
            server_1.shutdown();
            server_2.shutdown();
        }
    }

    /**
     * 使用nacos作为注册中心，且负载均衡策略为RoundRobin
     * @throws IOException
     */
    @Test
    public void example2() throws IOException {
        final String appName = "unit_test";

        // build servers
        final GrpcKitConfig serverConfig_1 = GrpcKitConfig.loadAndParse("grpc-kit-server-3.yml");
        final GrpcKitConfig serverConfig_2 = GrpcKitConfig.loadAndParse("grpc-kit-server-4.yml");

        final Server server_1 = GrpcKitServerBuilder.forConfig(appName, serverConfig_1)
            .addService(new GreeterService(1))
            .build();
        final Server server_2 = GrpcKitServerBuilder.forConfig(appName, serverConfig_2)
            .addService(new GreeterService(2))
            .build();

        // start servers
        server_1.start();
        server_2.start();

        try {
            // new channel and stub
            final GrpcKitConfig clientConfig = GrpcKitConfig.loadAndParse("grpc-kit-client-2.yml");
            final Channel channel = GrpcKitChannelBuilder.forConfig(appName, clientConfig)
                .usePlaintext()
                .build();
            final GreeterServiceGrpc.GreeterServiceBlockingStub stub = GrpcKitStubFactory.newStub(
                GreeterServiceGrpc.GreeterServiceBlockingStub.class, channel, clientConfig.validate());

            // send requests
            HelloRequest request = HelloRequest.newBuilder()
                .setName("Harrison")
                .build();
            for (int i = 0; i < 200; i++) {
                LOGGER.info("[Stub] greeting, name: {}", request.getName());
                HelloResponse response = stub.sayHello(request);
                LOGGER.info("[Stub] receive a new message: {}", response.getMessage());
            }
        } finally {
            // shutdown servers
            server_1.shutdown();
            server_2.shutdown();
        }
    }
}
