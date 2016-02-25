package ru.izebit.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.izebit.utils.NettyServer;


public class ResponseHandler extends SimpleChannelInboundHandler {
    private static final Logger LOGGER = LogManager.getLogger(ResponseHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error(cause);
        NettyServer.breakConnection(ctx, null, cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object o) throws Exception {

        ctx.close();
    }
}
