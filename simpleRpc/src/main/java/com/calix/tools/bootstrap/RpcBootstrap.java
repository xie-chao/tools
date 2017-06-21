package com.calix.tools.bootstrap;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by calix on 17-6-16.
 * RPC服务启动入口
 */
public class RpcBootstrap {

    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("spring-zk-rpc-server.xml");
    }
}
