package com.msb.Netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.ChannelGroupFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author:李罡毛
 * @date:2021/3/12 11:51
 */
public class Client_future {
    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        try {
            ChannelFuture channelFuture = bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ClientChannelInitializer())
                .connect("localhost", 8888)
                .sync();

            channelFuture.addListener((future)->{
                if (!future.isSuccess()) System.out.println("Not connected!!");
                else System.out.println("Connected successfully!!");
            });

            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
            System.exit(0);
        }
    }
}
