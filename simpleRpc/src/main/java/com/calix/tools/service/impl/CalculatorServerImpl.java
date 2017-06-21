package com.calix.tools.service.impl;


import com.calix.tools.RpcService;
import com.calix.tools.service.CalculatorServer;

import java.util.logging.Logger;

/**
 * Created by calix on 17-6-6.
 *
 */

@RpcService(CalculatorServer.class)
public class CalculatorServerImpl implements CalculatorServer {

    private static final Logger logger = Logger.getLogger(CalculatorServerImpl.class.getName());

    public static String welcomeMsg = "hello";

    public int calculate(int a, int b) {
        logger.info("hello : " + welcomeMsg);
        System.out.println("a : " + a + ", b : " + b);
        return a + b;
    }

    public String getDBData(long serialNum) {
//        RiskCommonData data = entityManager.find(RiskCommonData.class, serialNum);
//        if (data != null) {
//            return data.toString();
//        }
        return "";
    }
}
