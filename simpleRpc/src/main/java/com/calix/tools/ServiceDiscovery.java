package com.calix.tools;

import com.calix.tools.constant.ServerConstant;
import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by calix on 17-6-19.
 * 服务发现:连接ZK,添加watch事件
 */
public class ServiceDiscovery {

    private static final Logger logger = Logger.getLogger(ServiceDiscovery.class);

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private volatile List<String> datas = new ArrayList<>();

    private String registryAddress;

    public ServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;

        ZooKeeper zk = connectZookeeper();
        if (zk != null) {
            watchNode(zk);
        }
    }

    String discover() {
        String data = null;
        int size = datas.size();
        if (size > 0) {
            if (size == 1) {
                data = datas.get(0);
                logger.info("using only data : " + data);
            } else {
                data = datas.get(ThreadLocalRandom.current().nextInt(size));
                logger.info("using random data : " + data);
            }
        }
        return data;
    }

    private ZooKeeper connectZookeeper() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, ServerConstant.ZK_SESSION_TIMEOUT, watchedEvent -> {
                if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("发现服务异常！" + registryAddress, e.getCause());
        }
        return zk;
    }

    private void watchNode(final ZooKeeper zk) {
        try {
            List<String> nodes = zk.getChildren(ServerConstant.ZK_REGISTRY_PATH, watchedEvent -> {
                if (watchedEvent.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                    watchNode(zk);
                }
            });
            List<String> datas = new ArrayList<>();
            for (String node : nodes) {
                byte[] bytes = zk.getData(ServerConstant.ZK_REGISTRY_PATH + '/' + node, false, null);
                datas.add(new String(bytes));
            }
            logger.info("node data " + datas);
            this.datas = datas;
        } catch (KeeperException | InterruptedException e) {
            logger.error("获取信息失败", e.getCause());
        }
    }
}
