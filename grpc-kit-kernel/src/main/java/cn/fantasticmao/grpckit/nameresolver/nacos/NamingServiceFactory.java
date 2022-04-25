package cn.fantasticmao.grpckit.nameresolver.nacos;

import cn.fantasticmao.grpckit.GrpcKitException;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;

import java.util.Properties;

/**
 * @author harrison
 * @since 2022/4/21
 */
public class NamingServiceFactory {

    private NamingServiceFactory() { }

    public static NamingService get(String serverAddress, String namespaceId) {
        return newNameService(serverAddress, namespaceId);
    }

    private static NamingService newNameService(String serverAddress, String namespaceId) {
        try {
            Properties properties = new Properties();
            properties.setProperty("serverAddr", serverAddress);
            properties.setProperty("namespace", namespaceId);
            return NacosFactory.createNamingService(properties);
        } catch (NacosException e) {
            String errorMsg = String.format("create namingService error, serverAddr = %s, namespaceId = %s", serverAddress, namespaceId);
            throw new GrpcKitException(errorMsg);
        }
    }
}
