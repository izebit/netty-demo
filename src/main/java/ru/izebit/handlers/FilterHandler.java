package ru.izebit.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import ru.izebit.services.HttpServer;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * фильтрация запросов для данного ресурса
 * проверка параметров
 */
@Service
public class FilterHandler extends MessageToMessageDecoder<DefaultFullHttpRequest> {
    private static final Logger LOGGER = LogManager.getLogger(FilterHandler.class);
    private static final int MAX_LIMIT = 200_000;

    private static final String PREFIX_URL = "/weight";
    private static final Pattern URL_PATTERN_FILTER = Pattern.compile(PREFIX_URL + "(?:(?:/(\\d+)/(\\d+))|(?:\\?level=(\\d+)&index=(\\d+)))");

    @Override
    protected void decode(ChannelHandlerContext ctx, DefaultFullHttpRequest request, List<Object> out) throws Exception {

        if (request.getMethod() != HttpMethod.GET) {
            HttpServer.sendError(ctx, "метод к данному ресурсу не применим", HttpResponseStatus.NOT_ACCEPTABLE);
            return;
        }

        String url = request.getUri();
        url = url == null ? "" : url.toLowerCase();
        if (!url.startsWith(PREFIX_URL)) {
            HttpServer.sendError(ctx, "ресурс не найден", HttpResponseStatus.NOT_FOUND);
            return;
        }

        Matcher matcher = URL_PATTERN_FILTER.matcher(url);
        if (!matcher.find())
            HttpServer.sendError(ctx, "некорретно указаны параметры", HttpResponseStatus.BAD_REQUEST);

        try {
            int level = Integer.parseInt(matcher.group(1));
            int index = Integer.parseInt(matcher.group(2));

            if (level > MAX_LIMIT || index > MAX_LIMIT) {
                HttpServer.sendError(ctx, "значение параметров превышает допустимое", HttpResponseStatus.BAD_REQUEST);
                return;
            }
            if (index > level) {
                HttpServer.sendError(ctx, "level должен быть >= index", HttpResponseStatus.BAD_REQUEST);
                return;
            }

            request.headers().add("level", level);
            request.headers().add("index", index);
            out.add(request);
            request.retain();

        } catch (NumberFormatException e) {
            HttpServer.sendError(ctx, "неверный формат параметров", HttpResponseStatus.BAD_REQUEST);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error(cause);
        HttpServer.sendError(ctx, "ошибка сервера:" + cause.getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }
}
