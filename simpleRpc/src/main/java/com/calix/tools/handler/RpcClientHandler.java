package com.calix.tools.handler;

import com.calix.tools.param.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

/**
 * Created by calix on 17-6-16.
 * 请求处理
 */
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger logger = Logger.getLogger(RpcClientHandler.class);

    private final Object threadLock;

    public RpcClientHandler(Object threadLock) {
        this.threadLock = threadLock;
    }

    private RpcResponse response;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        this.response  =response;
        synchronized (threadLock) {
            threadLock.notifyAll();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause.getCause());
    }

    public RpcResponse getResponse() {
        return response;
    }
}
