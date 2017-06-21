package com.calix.tools;

import com.calix.tools.constant.ServerConstant;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by calix on 17-6-16.
 * 连接ZK注册中心，创建服务注册目录
 */
public class ServiceRegistry {

    private static final Logger logger = Logger.getLogger(ServiceRegistry.class);

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private String registryAddress;

    public ServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    void register(String data) {
        if (data != null) {
            ZooKeeper zk = connectZkServer();
            if (zk != null) {
                createNode(zk, data);
            }
        }
    }

    /**
     * 连接zookeeper进行服务注册
     */
    private ZooKeeper connectZkServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, ServerConstant.ZK_SESSION_TIMEOUT, watchedEvent -> {
                if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
        } catch (IOException | InterruptedException e) {
            logger.error("注册服务异常，ip:" + registryAddress, e.getCause());
        }
        return zk;
    }

    private void createNode(ZooKeeper zooKeeper, String data) {
        try {
            byte[] bytes = data.getBytes();
            String path = zooKeeper.create(ServerConstant.ZK_DATA_PATH,
                    bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            logger.info("create zookeeper node " + path + "#" + data);
        } catch (KeeperException | InterruptedException e) {
            logger.error("创建节点失败", e.getCause());
            e.printStackTrace();
        }
    }

}
