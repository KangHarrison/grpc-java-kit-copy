package cn.fantasticmao.grpckit;

import cn.fantasticmao.grpckit.support.NetUtil;
import cn.fantasticmao.grpckit.support.UriUtil;
import io.grpc.*;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.stub.AbstractStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Factory for the {@code gRPC Server, Stub and Channel}.
 *
 * @author fantasticmao
 * @version 1.39.0
 * @since 2022-04-03
 */
public class GrpcKitFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcKitFactory.class);

    private final GrpcKitConfig config;
    private final InetAddress localAddress;

    public GrpcKitFactory(@Nonnull String path) {
        Objects.requireNonNull(path, "Path must not be null");
        this.config = GrpcKitConfig.loadAndParse(path);
        this.config.validate();
        try {
            String preferInterface = this.config.getGrpc().getServer().getInterfaceName();
            this.localAddress = NetUtil.getLocalAddress(preferInterface);
        } catch (SocketException | UnknownHostException e) {
            throw new GrpcKitException("Get local address error", e);
        }
    }

    // server handling

    public Server newAndStartServer(BindableService... services) {
        final int port = config.getGrpc().getServer().getPort();
        ServerBuilder<?> serverBuilder = ServerBuilder
            .forPort(port);
        for (BindableService service : services) {
            serverBuilder.addService(service);
        }
        serverBuilder.addService(ProtoReflectionService.newInstance());
        Server server = serverBuilder.build();
        try {
            LOGGER.debug("Starting gRPC server on port: {}", port);
            server.start();
        } catch (IOException e) {
            throw new GrpcKitException("Start gRPC server error", e);
        }

        this.registerService();
        return server;
    }

    private void registerService() {
        final String appName = Objects.requireNonNull(config.getName(),
            "Name must not be null");
        final String appGroup = config.getGroup();
        final int serverPort = config.getGrpc().getServer().getPort();
        final int serverWeight = config.getGrpc().getServer().getWeight();
        final String serverTag = config.getGrpc().getServer().getTag();
        final String registry = Objects.requireNonNull(config.getNameResolver().getRegistry(),
            "Registry of nameResolver must not be null");

        final URI serviceUri = UriUtil.newServiceUri(URI.create(registry), appName, appGroup,
            localAddress, serverPort);
        final ServiceMetadata metadata = new ServiceMetadata(localAddress, serverPort, serverWeight,
            serverTag, Constant.VERSION);
        for (ServiceRegistryProvider provider : this.getAllServiceRegistries()) {
            try (ServiceRegistry serviceRegistry = provider.newServiceRegistry(serviceUri)) {
                if (serviceRegistry == null) {
                    continue;
                }
                boolean result = serviceRegistry.doRegister(metadata);
                if (!result) {
                    LOGGER.error("Register service failed for path: {}", serviceUri.getPath());
                }
            }
        }
    }

    private List<ServiceRegistryProvider> getAllServiceRegistries() {
        ServiceLoader<ServiceRegistryProvider> providers
            = ServiceLoader.load(ServiceRegistryProvider.class);
        return providers.stream()
            .map(ServiceLoader.Provider::get)
            .filter(ServiceRegistryProvider::isAvailable)
            .sorted()
            .collect(Collectors.toList());
    }

    // stub handling

    public <S extends AbstractStub<S>> S newStub(Class<S> clazz, Channel channel) {
        final AbstractStub.StubFactory<S> stubFactory = (_channel, callOptions) -> {
            final Constructor<S> constructor;
            try {
                // get private constructor
                constructor = clazz.getDeclaredConstructor(Channel.class, CallOptions.class);
                if ((!Modifier.isPublic(constructor.getModifiers()) ||
                    !Modifier.isPublic(constructor.getDeclaringClass().getModifiers())) && !constructor.canAccess(null)) {
                    constructor.setAccessible(true);
                }
            } catch (NoSuchMethodException e) {
                throw new GrpcKitException("Get gRPC stub constructor error", e);
            }

            try {
                return constructor.newInstance(_channel, callOptions);
            } catch (ReflectiveOperationException e) {
                throw new GrpcKitException("New gRPC stub instance error", e);
            }
        };

        final String methodName = "newStub";
        final Method method;
        try {
            method = clazz.getMethod(methodName, AbstractStub.StubFactory.class, Channel.class);
        } catch (NoSuchMethodException e) {
            throw new GrpcKitException("Get static method: " + methodName + " error", e);
        }

        final S stub;
        try {
            stub = clazz.cast(method.invoke(null, stubFactory, channel));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new GrpcKitException("Invoke static method: " + methodName + " error", e);
        }

        final String stubTag = config.getGrpc().getStub().getTag();
        final int stubTimeout = config.getGrpc().getStub().getTimeout();
        return stub.withOption(Constant.KEY_OPTION_TAG, stubTag)
            .withDeadlineAfter(stubTimeout, TimeUnit.MILLISECONDS);
    }

    // channel handling

    public Channel newChannel(String appName) {
        final String appGroup = config.getGroup();
        final String registry = Objects.requireNonNull(config.getNameResolver().getRegistry(),
            "Registry of nameResolver must not be null");
        final String policy = config.getLoadBalancer().getPolicy();
        final URI serviceUri = UriUtil.newServiceUri(URI.create(registry), appName, appGroup);
        return ManagedChannelBuilder
            .forTarget(serviceUri.toString())
            .userAgent(appName)
            .defaultLoadBalancingPolicy(ServiceLoadBalancer.Policy.of(policy).name)
            .usePlaintext()
            .build();
    }
}
