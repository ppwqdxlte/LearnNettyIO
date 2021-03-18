package com.tankNettyStudy.s1;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;

/**
 * @author:李罡毛
 * @date:2021/3/17 11:42
 */
public class Client {

    public static void main(String[] args) {
        Client client = new Client();
        client.connect();
    }

    private void connect(){
        EventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        ChannelFuture channelFuture = bootstrap.group(group).channel(NioSocketChannel.class).handler(new ClientChannelInitializer())
                .connect(new InetSocketAddress("127.0.0.1", 8888));
        try {
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) System.out.println("Connected successfully!");
                    else System.out.println("Not connected!");
                }
            }).sync();
            System.out.println("客户端已启动！");
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}

class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new ClientChannelHandler());
    }
}
class ClientChannelHandler extends ChannelInboundHandlerAdapter{
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("This client local-address is "+ctx.channel().localAddress()
                +",remote-address is "+ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(cause);
        ctx.close();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().close();
        System.out.println(ctx.channel().localAddress()+" shut down!");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(byteBuf.readerIndex(),bytes);
        System.out.println(ctx.channel().remoteAddress()+"--->>"+new String(bytes));
        byteBuf.clear();
        if (byteBuf != null && byteBuf.refCnt() > 0) ReferenceCountUtil.release(byteBuf);
    }
}
