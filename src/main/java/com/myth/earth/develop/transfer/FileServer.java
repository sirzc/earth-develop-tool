package com.myth.earth.develop.transfer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 文件传输服务
 *
 * @author zhouchao
 * @date 2024/7/3 上午8:32
 **/
public final class FileServer {

    private static EventLoopGroup bossGroup;
    private static EventLoopGroup workerGroup;

    public void run(int port, final String filePath) throws Exception {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 100)
         .handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {
             @Override
             public void initChannel(SocketChannel ch) throws Exception {
                 ChannelPipeline p = ch.pipeline();
                 p.addLast(new HttpRequestDecoder());
                 p.addLast(new HttpObjectAggregator(65536));
                 p.addLast(new HttpResponseEncoder());
                 p.addLast(new ChunkedWriteHandler(), new FileServerHandler(filePath));
             }
         });

        // Start the server.
        ChannelFuture f = b.bind(port).sync();
        // Wait until the server socket is closed.
        f.channel().closeFuture().sync();
        // System.out.println("执行完毕");
    }

    public void close() {
        if (bossGroup != null && !bossGroup.isShutdown()){
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null && !workerGroup.isShutdown()){
            workerGroup.shutdownGracefully();
        }
    }
}

