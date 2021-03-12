package com.msb.NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author:李罡毛
 * @date:2021/3/11 17:18
 */
public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress("127.0.0.1",8888));
        ssc.configureBlocking(false);
        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        while (true){
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()){
                SelectionKey key = iterator.next();
                iterator.remove();
                handle(key);
            }
        }

    }
    private static void handle(SelectionKey key){
        if (key.isAcceptable()){
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            try {
                SocketChannel sc = ssc.accept();
                sc.configureBlocking(false);
                ByteBuffer att = ByteBuffer.allocate(1024);
                sc.register(key.selector(),SelectionKey.OP_READ,att);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }else if (key.isReadable()){
            SocketChannel sc = (SocketChannel) key.channel();
            ByteBuffer att = (ByteBuffer) key.attachment();
            try {
                int len = sc.read(att);
                if (len == -1){
                    sc.close();
                    return;
                }
                if (len == 0) return;
                att.flip();
                byte[] bytes = new byte[att.limit()];
                att.get(bytes);
                System.out.println(sc.socket().getPort()+"----------->>"+new String(bytes));
                att.clear();
                //写出
                att = ByteBuffer.wrap(bytes);
                sc.write(att);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
