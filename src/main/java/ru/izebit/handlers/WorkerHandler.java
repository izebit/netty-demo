package ru.izebit.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.izebit.services.HttpServer;
import ru.izebit.services.SuperService;

import java.util.List;

/**
 * вычисление ответа
 */
@Service
public class WorkerHandler extends MessageToMessageDecoder<DefaultFullHttpRequest> {
    private static final Logger LOGGER = LogManager.getLogger(WorkerHandler.class);
    @Autowired
    private SuperService superService;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error(cause);
        HttpServer.sendError(ctx, "ошибка сервера:" + cause, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, DefaultFullHttpRequest msg, List<Object> out) throws Exception {
        int level = Integer.parseInt(msg.headers().get("level"));
        int index = Integer.parseInt(msg.headers().get("index"));


        Double result = superService.getHumanEdgeWeight(level, index);
        if (result == null) {
            HttpServer.sendError(
                    ctx,
                    "возникла непредвиденная ошибка во время вычисления результата",
                    HttpResponseStatus.INTERNAL_SERVER_ERROR);
            return;
        }

        out.add(result);
        msg.release();
    }
}
