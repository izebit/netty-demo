package ru.izebit.services;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.izebit.configuration.BeanConfiguration;

@Component
public class NettyServer implements HttpServer {
    private static final Logger LOGGER = LogManager.getLogger(NettyServer.class);

    /**
     * верняя граница скорости исходящего трафика байт/сек для одного канала
     */
    private static final int WRITE_LIMIT = 1_000;
    /**
     * верняя граница скорости входящего трафика байт/сек для одного канала
     */
    private static final int READ_LIMIT = 1_000;


    private ServerBootstrap server;
    private ChannelFuture channelFuture;

    @Autowired
    private BeanConfiguration beanConfiguration;

    @Override
    public void start(String address, int port) throws Exception {

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
                                    .addLast(beanConfiguration.getFilterHandler())
                                    .addLast(beanConfiguration.getWorkHandler())
                                    .addLast(beanConfiguration.getResponseHandler())
                                    .addLast(new ChannelTrafficShapingHandler(WRITE_LIMIT, READ_LIMIT));
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

    @Override
    public void stop() {
        server.group().shutdownGracefully();
        channelFuture = channelFuture.channel().close();
        channelFuture.awaitUninterruptibly();
    }
}
