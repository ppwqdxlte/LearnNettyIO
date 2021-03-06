package com.lgm.nettyNIO;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author:李罡毛
 * @date:2021/3/4 23:02
 */
public class NettyServerIO {

    public static void main(String[] args) {

        NioEventLoopGroup bosses = new NioEventLoopGroup(2);
        NioEventLoopGroup workers = new NioEventLoopGroup(2);
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        try{
            serverBootstrap.group(bosses,workers)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                                @Override
                                protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                                    ChannelPipeline pipeline = nioSocketChannel.pipeline();
                                    pipeline.addLast(new MyInbound());
                                }
                            });
            ChannelFuture syncFuture = serverBootstrap.bind(9093).sync();
            syncFuture.channel().closeFuture().sync();

        }catch (InterruptedException ie){
            ie.printStackTrace();
        }

    }



}
    class MyInbound extends ChannelInboundHandlerAdapter{

        /*

        对msg不处理，如果传输为文件，里面有许多换行符\n，那么就只识别为一个，发生【粘包】这件事

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ctx.write(msg);
            System.out.println("客户端说："+msg.toString());
        }*/

        /**
         * @param ctx
         * @param msg
         * @throws Exception
         *
         * 最简单的【解包】方法
         */
        @Override
        public void channelRead(ChannelHandlerContext ctx,Object msg) throws Exception{
            ByteBuf byteBufMsg = (ByteBuf) msg;
            int i = byteBufMsg.writerIndex();
            byte[] bytes = new byte[i];
            byteBufMsg.getBytes(0,bytes);
            String strMsg = new String(bytes);
            String[] strings = strMsg.split("\n");
            for (String s:
                 strings) {
                System.out.print("\t触发的命令:\t"+s);
            }

            ctx.write(msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

    }
