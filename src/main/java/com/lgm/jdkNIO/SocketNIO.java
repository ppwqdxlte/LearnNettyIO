package com.lgm.jdkNIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * @author:李罡毛
 * @date:2021/3/2 11:44
 * 所谓NIO就是一个线程可以处理多个客户端连接不阻塞，而BIO就是一个线程只能应对一个客户端连接
 * 如下代码所示，不管多少个客户端链接进来，都是一个线程搞定的
 */
public class SocketNIO {
    public static void main(String[] args) throws IOException, InterruptedException {

        List<SocketChannel> channels = new LinkedList<>();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(9090));
        serverSocketChannel.configureBlocking(false);//false-NIO;true-BIO

        while (true){
            Thread.sleep(1000);

            SocketChannel channel = serverSocketChannel.accept();
            if (channel == null){
                System.out.println("暂时没有客户端过来连接");
            }else {
                channels.add(channel);
                channel.configureBlocking(false);//false-NIO;true-BIO
                int port = channel.socket().getPort();
                System.out.println("客户端从端口"+port+"连接进来，就用此端口号代表该客户端~");
            }

            //缓冲区，可以在堆里，也可堆外，减少碎片，以及GC问题
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1028);
            for (SocketChannel c: channels) {   //串行化，一个个读，没有并发
                int num = c.read(byteBuffer);//读取到缓冲区中，position后移，>0:读到东西了 -1:任凭其空轮询内存就爆炸了 0:跳出不处理
                if (num>0){
                    byteBuffer.flip();//翻转，position移到首位，limit移到原position位置
                    byte[] bytes = new byte[byteBuffer.limit()];
                    byteBuffer.get(bytes);//从缓冲区写入到字节数组中
                    String str = new String(bytes);
                    System.out.println(c.socket().getPort()+"-------->>>>"+str);
                    byteBuffer.clear();//limit归位，缓冲区清空
                }else if (num == -1)continue;
            }
        }
    }
}
