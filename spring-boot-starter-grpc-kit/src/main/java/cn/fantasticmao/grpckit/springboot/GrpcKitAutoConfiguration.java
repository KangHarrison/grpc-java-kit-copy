package cn.fantasticmao.grpckit.springboot;

import cn.fantasticmao.grpckit.boot.GrpcKitConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * gRPC Kit Auto Configuration.
 *
 * 基于Java的容器配置
 *
 * @author fantasticmao
 * @version 1.39.0
 * @see <a href="https://docs.spring.io/spring-boot/docs/2.5.1/reference/html/features.html#features.developing-auto-configuration">Creating Your Own Auto-configuration</a>
 * @since 2022-04-03
 */
@Configuration
public class GrpcKitAutoConfiguration {
    @Value("${cn.fantasticmao.grpckit.config:grpc-kit.yml}")
    private String configPath;

    public static final String BEAN_NAME_GRPC_KIT_CONFIG = "_grpc_kit_config_";
    public static final String BEAN_NAME_GRPC_KIT_SERVER = "_grpc_kit_server_";

    @Bean(BEAN_NAME_GRPC_KIT_CONFIG)
    @ConditionalOnMissingBean
    public GrpcKitConfig grpcKitConfig() {
        return GrpcKitConfig.loadAndParse(configPath);
    }

    /**
     * 注入GrpcStubBeanPostProcessor，且依赖GrpcKitConfig
     *
     * @param config
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public GrpcStubBeanPostProcessor grpcStubBeanPostProcessor(@Autowired GrpcKitConfig config) {
        return new GrpcStubBeanPostProcessor(config);
    }
}
