package com.calix.tools;

import com.calix.tools.handler.RpcServiceHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by calix on 17-6-16.
 * 启动并注册服务
 */
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private static final Logger logger = Logger.getLogger(RpcServer.class);

    private String serverAddress;
    private ServiceRegistry serverRegistry;

    public static final Map<String, Object> handlerMapping = new HashMap<>();

    public RpcServer(String serverAddress, ServiceRegistry serviceRegistry) {
        this.serverAddress = serverAddress;
        this.serverRegistry = serviceRegistry;
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
        for (Object bean : serviceBeanMap.values()) {
            String interfaceName = bean.getClass().getAnnotation(RpcService.class).value().getName();
            handlerMapping.put(interfaceName, bean);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        startServer();
    }

    private void startServer() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap sb = new ServerBootstrap();
            sb.group(bossGroup, workerGroup);
            sb.channel(NioServerSocketChannel.class);
            sb.childHandler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel ch) throws Exception {
                    ObjectDecoder decoder = new ObjectDecoder(50 * 1024, ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader()));
                    ch.pipeline().addLast(decoder, new ObjectEncoder(), new RpcServiceHandler());
                }
            });
            sb.option(ChannelOption.SO_BACKLOG, 128);
            sb.childOption(ChannelOption.SO_KEEPALIVE, true);
            int port = Integer.parseInt(serverAddress.split(":")[1]);
            ChannelFuture cf = sb.bind(port).sync();
            logger.info("server started on port " + port);

            if (serverRegistry != null) {
                serverRegistry.register(serverAddress);
            }

            cf.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
