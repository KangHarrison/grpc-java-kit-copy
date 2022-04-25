package cn.fantasticmao.grpckit.loadbalancer;

import cn.fantasticmao.grpckit.ServiceLoadBalancer;
import cn.fantasticmao.grpckit.ServiceLoadBalancerProvider;
import io.grpc.LoadBalancer;

/**
 * @author harrison
 * @since 2022/4/24
 */
public class RoundRobinLoadBalancerProvider extends ServiceLoadBalancerProvider {
    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int getPriority() {
        return DEFAULT_PRIORITY + 2;
    }

    @Override
    public String getPolicyName() {
        return ServiceLoadBalancer.Policy.ROUND_ROBIN.name;
    }

    @Override
    public LoadBalancer newLoadBalancer(LoadBalancer.Helper helper) {
        return new RoundRobinLoadBalancer(helper);
    }
}
