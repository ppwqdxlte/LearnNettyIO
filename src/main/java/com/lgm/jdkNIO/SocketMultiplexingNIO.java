package com.lgm.jdkNIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * @author:李罡毛
 * @date:2021/3/2 16:33
 * jdk NIO 多路复用器 Selector
 * 单线程版本
 */
public class SocketMultiplexingNIO {

    public static void main(String[] args) {
        SocketMultiplexingNIO smn = new SocketMultiplexingNIO();
        smn.start();
    }

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private static final int PORT = 9091;

    private void initServer() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);

            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);//注册过程返回的是SelectionKey
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(){
        initServer();
        System.out.println("服务器启动了~~~~~~~~~~~~~~");
        try {
            while (true){
                if (selector.select(500) <= 0) continue;//问内核有没有事件！有几个！有就往下执行
                Set<SelectionKey> selectionKeys = selector.selectedKeys();//从多路复用器取出有效Key集合
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()){
                    SelectionKey next = iterator.next();
                    iterator.remove();//不移出的话复用器会爆炸
                    if (next.isAcceptable()){
                        acceptHandler(next);
                    }else if (next.isReadable()){
                        readHander(next);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readHander(SelectionKey next) throws IOException {
        SocketChannel clientChannel = (SocketChannel) next.channel();
        ByteBuffer byteBuffer  = (ByteBuffer) next.attachment();
        byteBuffer.clear();
        int read = 0;
        while (true){
            read = clientChannel.read(byteBuffer);
            if (read > 0){
                byteBuffer.flip();
              /*  while (byteBuffer.hasRemaining()){
                    clientChannel.write(byteBuffer);//原封不动发回客户端控制台了。
                }
                byteBuffer.clear();*/
                byte[] bytes = new byte[byteBuffer.limit()];
                byteBuffer.get(bytes);
                System.out.println("客户端"+clientChannel.socket().getPort()+"说："+new String(bytes));
                byteBuffer.clear();
            }else if (read == -1){
                clientChannel.close();
                System.out.println("@@@@@@@客户端"+clientChannel.socket().getPort()+"关闭了。。。");
                break;
            }else {
                break;
            }
        }
    }

    private void acceptHandler(SelectionKey next) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) next.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        ByteBuffer byteBuffer = ByteBuffer.allocate(4096);//堆里面
        clientChannel.register(selector,SelectionKey.OP_READ,byteBuffer);
        System.out.println("客户端"+clientChannel.socket().getPort()+"已连接");
    }
}
