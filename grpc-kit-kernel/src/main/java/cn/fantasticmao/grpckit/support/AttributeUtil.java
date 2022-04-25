package cn.fantasticmao.grpckit.support;

import io.grpc.Attributes;
import io.grpc.ConnectivityStateInfo;
import io.grpc.EquivalentAddressGroup;
import io.grpc.LoadBalancer;

import javax.annotation.Nonnull;

/**
 * An util class for {@link Attributes gRPC Attributes}.
 *
 * @author fantasticmao
 * @version 1.39.0
 * @since 2022-03-27
 */
public interface AttributeUtil {
    Attributes.Key<Integer> KEY_WEIGHT = Attributes.Key.create("weight");
    Attributes.Key<String> KEY_TAG = Attributes.Key.create("tag");

    /**
     * Get the value of a specific key in the {@link Attributes} of a {@link EquivalentAddressGroup}.
     *
     * @param <T> type of value
     * @param addressGroup addressGroup
     * @param key key
     * @return the value of key
     */
    @Nonnull
    static <T> T getAttribute(EquivalentAddressGroup addressGroup, Attributes.Key<T> key) {
        T attribute = addressGroup.getAttributes().get(key);
        if (attribute == null) {
            String message = String.format("Attribute '%s' in the addressGroup '%s' must not be null.", key, addressGroup);
            throw new NullPointerException(message);
        }
        return attribute;
    }

    /**
     * Keep the {@link ValRef value reference} in a {@link Attributes} of the
     * {@link LoadBalancer.Subchannel}, so that it can be modified.
     */
    Attributes.Key<ValRef<Integer>> KEY_REF_WEIGHT = Attributes.Key.create("weight");
    Attributes.Key<ValRef<String>> KEY_REF_TAG = Attributes.Key.create("tag");
    Attributes.Key<ValRef<ConnectivityStateInfo>> KEY_REF_STATE = Attributes.Key.create("state");

    /**
     * Get the {@link ValRef} of a specific key in a {@link Attributes} of the {@link LoadBalancer.Subchannel}.
     *
     * @param <T> type of value
     * @param subChannel subChannel
     * @param key key
     * @return the value of key
     */
    @Nonnull
    static <T> ValRef<T> getValRef(LoadBalancer.Subchannel subChannel, Attributes.Key<ValRef<T>> key) {
        ValRef<T> ref = subChannel.getAttributes().get(key);
        if (ref == null) {
            String message = String.format("Attribute '%s' in the subChannel '%s' must not be null.", key, subChannel);
            throw new NullPointerException(message);
        }
        return ref;
    }
}
