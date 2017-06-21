package com.calix.tools.constant;

/**
 * Created by calix on 17-6-16.
 * 常量
 */
public interface ServerConstant {

    int ZK_SESSION_TIMEOUT = 5000;

    String ZK_REGISTRY_PATH = "/rpcRegistry";
    String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";
}
