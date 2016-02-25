package ru.izebit.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.izebit.utils.NettyServer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * фильтрация запросов для данного ресурса
 * проверка параметров
 */
public class FilterHandler extends SimpleChannelInboundHandler {
    private static final Logger LOGGER = LogManager.getLogger(FilterHandler.class);
    private static final Pattern URL_PATTERN_FILTER = Pattern.compile("/weight(?:(?:/(\\d+)/(\\d+))|(?:\\?level=(\\d+)&index=(\\d+)))");

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof DefaultFullHttpRequest)) {
            NettyServer.breakConnection(ctx, null);
            return;
        }
        DefaultFullHttpRequest request = (DefaultFullHttpRequest) msg;

        if (request.getMethod() != HttpMethod.GET) {
            NettyServer.breakConnection(ctx, "метод к данному ресурсу не применим");
            return;
        }

        String url = request.getUri();
        url = url == null ? "" : url;
        Matcher matcher = URL_PATTERN_FILTER.matcher(url);
        if (!matcher.find())
            NettyServer.breakConnection(ctx, "некорретно указаны параметры");

        try {
            int level = Integer.parseInt(matcher.group(1));
            int index = Integer.parseInt(matcher.group(2));

            if (level > 1_000 || index > 1_000) {
                NettyServer.breakConnection(ctx, "значение параметров превышает допустимое");
                return;
            }
            if (index > level) {
                NettyServer.breakConnection(ctx, "level должен быть >= index");
                return;
            }


            request.headers().add("level", level);
            request.headers().add("index", index);

        } catch (NumberFormatException e) {
            NettyServer.breakConnection(ctx, "неверный формат параметров");
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error(cause);
        NettyServer.breakConnection(ctx, null, cause);
    }
}
