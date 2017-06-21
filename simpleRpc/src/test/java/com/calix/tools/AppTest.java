package com.calix.tools;

import com.calix.tools.service.CalculatorServer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @org.junit.Test
    public void testRpc() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-zk-rpc-client.xml");
        RpcProxy rpcProxy = context.getBean(RpcProxy.class);
        CalculatorServer server = rpcProxy.lookup(CalculatorServer.class);
        int result = server.calculate(1, 2);
        System.out.println(result);
    }
}
