package ru.izebit.utils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.izebit.handlers.FilterHandler;
import ru.izebit.handlers.RequestHandler;
import ru.izebit.handlers.ResponseHandler;

import java.nio.charset.StandardCharsets;

public class NettyServer {
    private static final Logger LOGGER = LogManager.getLogger(NettyServer.class);

    private final String address;
    private final int port;

    private ServerBootstrap server;
    private ChannelFuture channelFuture;

    public NettyServer(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public static void breakConnection(ChannelHandlerContext ctx, String errorMessage, Throwable... exception) {
        FullHttpResponse response;
        if (errorMessage == null) {

            String errorText = "ошибка на сервере ";
            if (exception.length == 1) errorText += ":" + exception[0].getMessage();

            ByteBuf content = Unpooled.copiedBuffer(errorText, StandardCharsets.UTF_8);
            response =
                    new DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1,
                            HttpResponseStatus.INTERNAL_SERVER_ERROR,
                            content);
        } else {

            ByteBuf content = Unpooled.copiedBuffer(errorMessage, StandardCharsets.UTF_8);
            response =
                    new DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1,
                            HttpResponseStatus.BAD_REQUEST,
                            content);
        }

        response.headers().set(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaders.Names.ACCEPT_CHARSET, StandardCharsets.UTF_8.name());


        ChannelFuture channelFuture = ctx.writeAndFlush(response);
        channelFuture.addListener(ChannelFutureListener.CLOSE);
    }

    public void start() throws InterruptedException {
        LOGGER.info("запуск сервера...");
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        try {
            server = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        private final CorsConfig corsConfig = CorsConfig
                                .anyOrigin()
                                .allowNullOrigin()
                                .allowCredentials().build();

                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new HttpResponseEncoder())
                                    .addLast(new HttpRequestDecoder())
                                    .addLast(new HttpObjectAggregator(Integer.MAX_VALUE))
                                    .addLast(new CorsHandler(corsConfig))
                                    .addLast(new FilterHandler())
                                    .addLast(new RequestHandler())
                                    .addLast(new ResponseHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 500)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            channelFuture = server.bind(address, port).sync();

            LOGGER.info("сервер запущен на {}:{}", address, port);
            channelFuture.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
            channelFuture.channel().close().awaitUninterruptibly();
        }
    }

    public void stop() {
        server.group().shutdownGracefully();
        channelFuture = channelFuture.channel().close();
        channelFuture.awaitUninterruptibly();
    }

}
