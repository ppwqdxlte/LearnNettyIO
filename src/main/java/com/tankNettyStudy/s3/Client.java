package com.tankNettyStudy.s3;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

/**
 * @author:李罡毛
 * @date:2021/3/17 15:13
 */
public class Client {

    private Channel channel;

    public void connect(){
        EventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        ChannelFuture channelFuture = bootstrap.group(group).channel(NioSocketChannel.class).handler(new ClientChannelInitializer())
                .connect("127.0.0.1", 8888);
        try {
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) System.out.println("Connect seccessfully!");
                    else System.out.println("Fail to connect");
                    channel = channelFuture.channel();
                }
            }).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public void sentTextToServer(String text) {
        ByteBuf byteBuf = Unpooled.copiedBuffer(text.getBytes());
        channel.writeAndFlush(byteBuf);
    }

    public void shutDown(){
        channel.close();
    }
}

class ClientChannelInitializer extends ChannelInitializer<SocketChannel>{

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new ClientChannelHandler());
    }
}

class ClientChannelHandler extends ChannelInboundHandlerAdapter{
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端"+ctx.channel().localAddress()+"已启动，成功连接"+ctx.channel().remoteAddress());
        ctx.writeAndFlush("Hello~".getBytes());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(cause.getMessage());
        ctx.close();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().close();
        System.out.println(ctx.channel().localAddress()+" shut down!");
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(byteBuf.readerIndex(),bytes);
        System.out.println(ctx.channel().remoteAddress()+"说："+new String(bytes));
        if (byteBuf!=null&&byteBuf.refCnt()>0) ReferenceCountUtil.release(msg);
    }

}