package com.msb.Netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

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
                .option(ChannelOption.SO_BACKLOG,  1024 )
                .option(ChannelOption.SO_RCVBUF,  1024 * 256 )
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
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
class ClientChannelHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = null;
        try{
            byteBuf = ((ByteBuf) msg);
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.getBytes(byteBuf.readerIndex(),bytes);
            System.out.println("服务器返回："+new String(bytes));
            byteBuf.clear();
        } finally {
            if (byteBuf != null && byteBuf.refCnt() > 0) ReferenceCountUtil.release(byteBuf);
        }
    }
}
