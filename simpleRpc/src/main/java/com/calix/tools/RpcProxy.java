package com.calix.tools;

import com.calix.tools.param.RpcRequest;
import com.calix.tools.param.RpcResponse;
import net.sf.cglib.proxy.Proxy;

import java.util.UUID;

/**
 * Created by calix on 17-6-19.
 * RPC调用代理
 */
public class RpcProxy {

    private String serverAddress;
    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @SuppressWarnings("unchecked")
    public <T> T lookup(Class<?> interfaceClass) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, (proxy, method, args) -> {
            RpcRequest request = new RpcRequest();
            request.setRequestId(UUID.randomUUID().toString());
            request.setClassName(method.getDeclaringClass().getName());
            request.setMethodName(method.getName());
            request.setParameterTypes(method.getParameterTypes());
            request.setParameters(args);

            if (serviceDiscovery != null) {
                serverAddress = serviceDiscovery.discover(); // 发现服务
            }
            String[] addresss = serverAddress.split(":");
            String host = addresss[0];
            int port = Integer.parseInt(addresss[1]);
            RpcClient rpcClient = new RpcClient(host, port);
            RpcResponse response = rpcClient.send(request);

            if (response.getErrorMsg() != null) {
                throw new Exception(response.getErrorMsg());
            }
            return response.getResult();
        });
    }
}
