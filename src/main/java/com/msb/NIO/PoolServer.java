package com.msb.NIO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author:李罡毛
 * @date:2021/3/11 18:14
 */
public class PoolServer {

    private ExecutorService pool = Executors.newCachedThreadPool();
    private Selector selector;

    public static void main(String[] args) throws IOException {
        PoolServer server = new PoolServer();
        server.init(8889);
        server.listen();
    }

    private void init(int port) throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress("127.0.0.1",port));
        ssc.configureBlocking(false);
        selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器已启动");
    }

    private void listen() throws IOException {
        while (true){
            selector.select(300);
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()){
                SelectionKey key = iterator.next();
                iterator.remove();//!!!
                if (key.isAcceptable()){
                    ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                    SocketChannel client = ssc.accept();
                    client.configureBlocking(false);
                    client.register(selector,SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }else if (key.isReadable()){
                    key.interestOps(key.interestOps()&(~SelectionKey.OP_READ));//相当于iterator.remove()...
                    pool.execute(new ThreadHandlerChannel(key));
                }
            }
        }
    }

    private class ThreadHandlerChannel extends Thread{
        private SelectionKey key;
        public ThreadHandlerChannel(SelectionKey key){
            this.key = key;
        }
        @Override
        public void run(){
            SocketChannel client = (SocketChannel) key.channel();
            ByteBuffer attachment = (ByteBuffer) key.attachment();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                int len = 0;
                while ((len = client.read(attachment))>0){
                    attachment.flip();
                    baos.write(attachment.array(),0,len);
                    attachment.clear();
                }
                baos.close();
                //
                if (baos.toByteArray().length > 0){
                    System.out.println(client.socket().getPort()+"---------->>>"+new String(baos.toByteArray()));
                    attachment = ByteBuffer.wrap(baos.toByteArray());
                    client.write(attachment);
                    attachment.clear();
                }
                if (len == -1){
                    client.close();
                    System.out.println("客户端"+client.socket().getPort()+"已关闭！");
                }else {
                    key.interestOps(key.interestOps()|SelectionKey.OP_READ);//指定【读】这个事儿
                    key.selector().wakeup();//唤醒多路复用器继续监听
                }

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
