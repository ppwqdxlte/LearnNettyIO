package com.lgm.nettyNIO;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.charset.StandardCharsets;

/**
 * @author:李罡毛
 * @date:2021/3/6 18:08
 */
public class NettyClientIO {
    public static void main(String[] args) {
        try{
            NioEventLoopGroup worker = new NioEventLoopGroup(1);
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(worker)
                    .channel(NioSocketChannel.class)
                    .remoteAddress("localhost",9093)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            System.out.println("初始化客户端");
                            ChannelPipeline pipeline = nioSocketChannel.pipeline();
                            pipeline.addLast(new MyInbound());//和服务器公用一个Inbound，发来发去无限循环，刚开始谁都不说话，
                        }
                    });
            ChannelFuture syncConn = bootstrap.connect().sync();
            Channel client = syncConn.channel();
            System.out.println(client);
            ByteBuf byteBuf = Unpooled.copiedBuffer("Hello world!".getBytes());//客户端说了一句就疯狂来往输出
            client.writeAndFlush(byteBuf).sync();

        }catch (InterruptedException ie){
            ie.printStackTrace();
        }
    }
}
