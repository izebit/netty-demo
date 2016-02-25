package ru.izebit.services;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;

public interface HttpServer {

    static void sendError(ChannelHandlerContext ctx, String errorMessage, HttpResponseStatus status) {
        ByteBuf content = Unpooled.copiedBuffer(errorMessage, StandardCharsets.UTF_8);
        FullHttpResponse response =
                new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        status,
                        content);


        response.headers().set(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaders.Names.ACCEPT_CHARSET, StandardCharsets.UTF_8.name());


        ChannelFuture channelFuture = ctx.writeAndFlush(response);
        channelFuture.addListener(ChannelFutureListener.CLOSE);
    }

    void start(String address, int port) throws Exception;

    void stop();
}
