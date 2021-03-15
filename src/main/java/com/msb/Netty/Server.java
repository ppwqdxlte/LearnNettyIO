package com.msb.Netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.ReferenceCountUtil;

/**
 * @author:李罡毛
 * @date:2021/3/12 17:27
 * ByteBuf 效率很高，因为网络数据先走操作系统内存，再复制到JVM里面一份儿，
 * 而ByteBuf直接从内存读取，java 1.几之后实现可以从
 * 操作系统的内存直接读取 【Directory memory】
 * 【但是】直接访问内存就直接跳过了Java的垃圾回收机制，用的越来越多就会占用更多系统内存，
 * 就需要释放！！！writeAndFlush()断开byteBuf的指向，什么时候真正释放系统内存那块呢？
 */
public class Server {
    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(2);//2个线程，并非2个线程池！搞清楚！
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            ChannelFuture future = serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new ServerChildHandler());
                        System.out.println("initChildChannel()--->>>"+Thread.currentThread().getId());
                    }
                })
                .option(ChannelOption.SO_BACKLOG,  1024 )
                .option(ChannelOption.SO_RCVBUF,  1024 * 256 )
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.SO_KEEPALIVE,  true )
                .bind(8888)
                .sync();

            System.out.println("Server started!!!");

            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}

class ServerChildHandler extends ChannelInboundHandlerAdapter{//SimpleChannelInboundHandler Codec两个结合有泛型
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ByteBuf byteBuf = Unpooled.copiedBuffer("Hello1".getBytes());
        ctx.writeAndFlush(byteBuf);//writeAndFlush砍掉byteBuf对系统内存的指向！
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IllegalReferenceCountException) {
            System.out.println("不处理了");
        }else {
            System.out.println("也不知道啥问题，出在哪了，，"+cause.getCause());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = null;
        try {
            byteBuf = ((ByteBuf) msg).retain();//System.out.println(byteBuf); System.out.println(byteBuf.refCnt());//1
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.getBytes(byteBuf.readerIndex(),bytes);
            System.out.println(new String(bytes).strip());
            ctx.writeAndFlush(byteBuf);
        } finally {
            if (byteBuf!=null && byteBuf.refCnt()>0) {
                ReferenceCountUtil.release(byteBuf);//System.out.println(byteBuf.refCnt());//0  打印出来了，确实释放了System.out.println("------");
            }
        }

    }
}