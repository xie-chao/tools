package com.calix.tools.handler;

import com.calix.tools.RpcServer;
import com.calix.tools.param.RpcRequest;
import com.calix.tools.param.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import org.apache.log4j.Logger;

/**
 * Created by calix on 17-6-16.
 * 请求处理
 */
public class RpcServiceHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger logger = Logger.getLogger(RpcServiceHandler.class);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        RpcResponse response = new RpcResponse();
        try {
            Object result = handle(request);
            response.setResult(result);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t.getCause());
        }

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private Object handle(RpcRequest request) throws Throwable {
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());

        String className = request.getClassName();

        Object result = null;
        try {
            Object bean = RpcServer.handlerMapping.get(className);
            FastClass fastClass = FastClass.create(bean.getClass());
            result = fastClass.invoke(request.getMethodName(), request.getParameterTypes(), bean, request.getParameters());
        } catch (Exception e) {
            logger.error(e.getMessage(), e.getCause());
            response.setErrorMsg(e.getMessage());
        }

        return result;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause.getCause());
    }

}
