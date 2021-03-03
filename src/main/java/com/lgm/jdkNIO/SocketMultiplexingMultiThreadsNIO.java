package com.lgm.jdkNIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author:李罡毛
 * @date:2021/3/3 22:14
 * JDK NIO 多路复用器
 * 多线程版本
 * 每一条线程运行一个多路复用器
 * 我用new Random(seed).nextInt() %2 == 0理论上可以随机指定selector，
 * 但是代码运行过程中创建几个客户端连接都是绑定一个复用器了，负载均衡肯定有更好的方法，
 * 此demo仅仅展现多线程多路复用而已，不作过多要求
 */
public class SocketMultiplexingMultiThreadsNIO {

    public static void main(String[] args) throws IOException {
        new SocketMultiplexingMultiThreadsNIO().start();
    }

    private ServerSocketChannel serverSocketChannel;
    private Selector selector1;
    private Selector selector2;
    private Selector selector3;
    private static final int PORT = 9092;
    private static final int FREQUENCY = 200;//selector轮询频率
    private static AtomicInteger workerNumber = new AtomicInteger(0);//worker工号

    private void initServer() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(PORT));
        serverSocketChannel.configureBlocking(false);

        selector1 = Selector.open();
        selector2 = Selector.open();
        selector3 = Selector.open();

        serverSocketChannel.register(selector1, SelectionKey.OP_ACCEPT);
    }

    public void start() throws IOException {
        initServer();
        NioThread t1 = new NioThread(selector1,2);
        NioThread t2 = new NioThread(selector2);
        NioThread t3 = new NioThread(selector3);
        t1.start();
        t2.start();
        t3.start();
        System.out.println("服务器启动了。。。");
    }

    private void bossHandler(Selector selector) throws IOException {
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = selectionKeys.iterator();
        while (iterator.hasNext()){
            SelectionKey next = iterator.next();
            iterator.remove();
            if (next.isAcceptable()) acceptHandler(next);
        }
    }

    private void workerHandler(Selector selector) throws IOException {
        Set<SelectionKey> selectionKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = selectionKeys.iterator();
        while (iterator.hasNext()){
            SelectionKey next = iterator.next();
            iterator.remove();
            if (next.isReadable()) readHandler(next);
        }
    }

    private void acceptHandler(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
        SocketChannel clientChannel = ssc.accept();
        clientChannel.configureBlocking(false);
        ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
        Selector workerSelector = new Random(9).nextInt()%2 == 0 ?
                selector3 : selector2;//随机注册，负载均衡。。哈哈哈
        clientChannel.register(workerSelector,SelectionKey.OP_READ,byteBuffer);
        System.out.println("客户端"+clientChannel.socket().getPort()+"已连接");
    }

    private void readHandler(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
        byteBuffer.clear();
        int read = 0;
        while (true){
            read = clientChannel.read(byteBuffer);
            if (read > 0){
                byteBuffer.flip();
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

    private class NioThread extends Thread{

        private Selector s;
        private int workerCount;
        private int workerNum;//工号

        public NioThread(Selector s,int workerCount){
            this.s = s;
            this.workerCount = workerCount;
            System.out.println("Boss启动了");
        }

        public NioThread(Selector s){
            this.workerNum = workerNumber.incrementAndGet();
            this.s = s;
            System.out.println("worker"+this.workerNum+"启动了");
        }

        @Override
        public void run(){
            super.run();
            try{
                while (true){
                    int select = s.select(FREQUENCY);
                    if (select <= 0) continue;
                    if (workerCount > 0) {
                        bossHandler(selector1);
                    }else {
                        System.out.println("worker"+this.workerNum+">>>");
                        workerHandler(s);
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        @Override
        public void start(){
            super.start();
        }
    }
}
