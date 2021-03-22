package com.tankNettyStudy.s5;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author:李罡毛
 * @date:2021/3/22 10:09
 */
public class Server {

    public static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public static Channel channel;

    public void start(){
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(2);
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        try {
            ChannelFuture channelFuture = serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .handler(new ServerChannelInitializer())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new ServerChildChannelHandler());
                            System.out.println("initChildChannel()--->>>" + pipeline.channel().remoteAddress());
                        }
                    }).option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_RCVBUF, 1024 * 256)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .bind(8888).sync();

            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public void shutDown(){
        channel.close();
        System.exit(0);
    }
}

class ServerChannelInitializer extends ChannelInitializer<ServerChannel>{

    @Override
    protected void initChannel(ServerChannel serverChannel) throws Exception {
        Server.channel = serverChannel;
        System.out.println("Server is started!");
        ServerFrame.INSTANCE.updateTaLeft(("Server is started!"+System.lineSeparator()+serverChannel).getBytes());
    }
}

class ServerChildChannelHandler extends ChannelInboundHandlerAdapter{
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ByteBuf byteBuf = Unpooled.copiedBuffer("Hello client,I'm Server!".getBytes());
        ctx.writeAndFlush(byteBuf);//writeAndFlush砍掉byteBuf对系统内存的指向！
        //添加client到ChannelGroup中
        Server.clients.add(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IllegalReferenceCountException) {
            System.out.println("不处理了");
        }else {
            System.out.println("也不知道啥问题，出在哪了，，"+cause);
        }
        ctx.close();//记录下异常，必须关闭掉该ctx，客户端阻塞解除，对面就正式close了
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        if (!ctx.channel().isActive()) {
            Server.clients.remove(ctx.channel());
            ctx.channel().close();
            System.out.println(ctx.channel().remoteAddress()+"shut down!");
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = null;
        try {
            byteBuf = ((ByteBuf) msg).retain();//System.out.println(byteBuf); System.out.println(byteBuf.refCnt());//1
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.getBytes(byteBuf.readerIndex(),bytes);
            System.out.println(ctx.channel().remoteAddress()+"说："+new String(bytes).strip());
            //哪个客户端有消息，挨着排儿转发出去
            Server.clients.writeAndFlush(byteBuf);
            ServerFrame.INSTANCE.updateTaRight(bytes);
        } finally {
            if (byteBuf!=null && byteBuf.refCnt()>0) {
                ReferenceCountUtil.release(byteBuf);//System.out.println(byteBuf.refCnt());//0  打印出来了，确实释放了System.out.println("------");
            }
        }

    }
}
