package com.msb.AIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * @author:李罡毛
 * @date:2021/3/11 23:07
 */
public class Server {
    public static void main(String[] args) throws IOException, InterruptedException {
        final AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open()
                .bind(new InetSocketAddress("localhost",8888));
        assc.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {

            @Override
            public void completed(AsynchronousSocketChannel client, Object attachment) {
                // 处理下一次链接，否则服务器ServerSocket只能接收一次连接请求，无法接收其它client连接请求
                assc.accept(null,this);
                try {
                    System.out.println(client.getRemoteAddress()+"连接进来了...");
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    client.read(byteBuffer, byteBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer readResult, ByteBuffer attachment) {
                            try {
                                if (readResult < 0){
                                    System.out.println(client.getRemoteAddress().toString()+"已关闭");
                                    client.close();
                                    return;
                                }
                                if (readResult > 0){
                                    attachment.flip();
                                    byte[] bytes = new byte[readResult];
                                    attachment.get(bytes);
                                    String[] msges = new String(bytes,0,readResult).split("\n");
                                    String msg = "";
                                    for (int i = 0; i < msges.length; i++) {
                                        msg = msg.concat(msges[i].strip());
                                    }
                                    if (msg.length() > 0){
                                        System.out.println(client.getRemoteAddress().toString()+"--------->>>"+msg);
                                        client.write(ByteBuffer.wrap("hello,client!".getBytes()));
                                    }
                                    attachment.clear();
                                }
                                // 处理完之后，要继续监听read,否则同一个socket只能通信一次，无法接收到之后通过socket发送的消息
                                // -------------  重要 -------------------
                                client.read(attachment,attachment,this);
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {

                        }
                    });

                    while (true){
                        Thread.sleep(300);
                    }
                } catch (IOException | InterruptedException ioException) {
                    ioException.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
            }
        });

        while (true){
            Thread.sleep(300);
        }
    }
}
