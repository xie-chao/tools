package com.calix.tools;

import com.calix.tools.handler.RpcClientHandler;
import com.calix.tools.param.RpcRequest;
import com.calix.tools.param.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.apache.log4j.Logger;

/**
 * Created by calix on 17-6-19.
 * RPC客户端
 */
class RpcClient {

    private static final Logger logger = Logger.getLogger(RpcClient.class);

    private final Object threadLock = new Object();

    private String host;
    private int port;

    RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    RpcResponse send(RpcRequest request) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap bs = new Bootstrap();
            bs.group(workerGroup);
            bs.channel(NioSocketChannel.class);
            bs.option(ChannelOption.SO_KEEPALIVE, true);
            ObjectDecoder decoder = new ObjectDecoder(50 * 1024, ClassResolvers.cacheDisabled(getClass().getClassLoader()));
            RpcClientHandler clientHandler = new RpcClientHandler(threadLock);
            bs.handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(decoder, new ObjectEncoder(), clientHandler);
                }
            });
            logger.info("connecting " + host + ":" + port);
            ChannelFuture cf = bs.connect(host, port).sync();
            cf.channel().writeAndFlush(request).sync();

            lockThread();

            cf.channel().closeFuture().sync();

            return clientHandler.getResponse();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    private void lockThread() throws InterruptedException {
        synchronized (threadLock) {
            threadLock.wait();
        }
    }
}
