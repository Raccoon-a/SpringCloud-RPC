package cn.rylan.rpc.netty.model;

import cn.rylan.rest.model.ServiceInstance;

public class ChannelRecord {

    private String serviceName;

    private ServiceInstance serviceInstance;

    public ChannelRecord() {
    }

    private ChannelRecord(Builder builder) {
        setServiceName(builder.serviceName);
        setServiceInstance(builder.serviceInstance);
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }


    public static final class Builder {
        private String serviceName;
        private ServiceInstance serviceInstance;

        public Builder() {
        }

        public Builder serviceName(String val) {
            serviceName = val;
            return this;
        }

        public Builder serviceInstance(ServiceInstance val) {
            serviceInstance = val;
            return this;
        }

        public ChannelRecord build() {
            return new ChannelRecord(this);
        }
    }
}
