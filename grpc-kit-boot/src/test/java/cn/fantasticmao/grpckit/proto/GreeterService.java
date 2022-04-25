package cn.fantasticmao.grpckit.proto;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GreeterService
 *
 * @author fantasticmao
 * @version 1.39.0
 * @since 2021-07-31
 */
public class GreeterService extends GreeterServiceGrpc.GreeterServiceImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(GreeterService.class);

    private final int num;

    public GreeterService(int num) {
        this.num = num;
    }

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        LOGGER.info("[Server {}] receive a new message, name: {}", this.num, request.getName());

        HelloResponse response = HelloResponse.newBuilder()
            .setMessage("Hello " + request.getName())
            .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
