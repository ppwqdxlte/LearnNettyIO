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
 * 但是存在【资源浪费问题】，有没有客户端我服务器每次都要accept一下，客户端有没有消息，我也要read一下。。。
 * 如果每一个连接相当于一条路，那么每条路都要看一眼，
 * c10k容量的连接数，就相当于 o(c10k)的复杂度，如果有一个设备，接管这多连接的处理，不用你每条路都看一眼，
 * 它就是【多路复用器】
 * 【主要监听IO状态，相当于收费站告诉你哪条路来车了，查车还得是线程自己的事儿，只不过不用挨个跑一趟啦
 * R\W依然是程序自己触发的】
 * 【凡是程序自己触发的，那都算【【【【【同步】】】】】，那它原理是撒呢？
 * 内核暴露给用户的接口有select，poll，epoll，kqueue等等
 * socket() = 6fd
 * find(6fd,9090)
 * listen(6fd)
 * select(6fd)
 * accept(6fd)==7fd
 * select(6fd,7fd.......略略略)第一个是ServerSocket,后面全是客户端对象，这么多对象监听状态的任务一口气丢给内核，整个复杂度才o(几)而已
 *
 */
public class SocketNIO {
    public static void main(String[] args) throws IOException, InterruptedException {

        List<SocketChannel> channels = new LinkedList<>();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(9089));
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
