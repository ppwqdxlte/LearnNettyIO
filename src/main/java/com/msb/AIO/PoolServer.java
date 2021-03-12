package com.msb.AIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author:李罡毛
 * @date:2021/3/12 9:15
 */
public class PoolServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        ExecutorService pool = Executors.newCachedThreadPool();
        AsynchronousChannelGroup threadGroup = AsynchronousChannelGroup.withCachedThreadPool(pool, 1);

        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open(threadGroup)
                .bind(new InetSocketAddress(8888));

        assc.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {

            @Override
            public void completed(AsynchronousSocketChannel client, Object asscAttachment) {
                assc.accept(null,this);
                try {
                    System.out.println(client.getRemoteAddress().toString()+"连接进来了~");
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    client.read(byteBuffer, byteBuffer, new CompletionHandler<Integer, Object>() {

                        @Override
                        public void completed(Integer result, Object attachment) {
                            ByteBuffer atta = (ByteBuffer) attachment;
                            try {
                                if (result < 0){
                                    String clientAdd = client.getRemoteAddress().toString();
                                    client.close();
                                    System.out.println(clientAdd+"已关闭！");
                                    return;
                                }
                                if (result > 0){
                                    atta.flip();
                                    byte[] bytes = new byte[result];
                                    atta.get(bytes);
                                    String[] split = new String(bytes).split("\n");
                                    String msg = "";
                                    for (int i = 0; i < split.length; i++) {
                                        msg = msg.concat(split[i].strip());
                                    }
                                    if (msg.length() > 0) System.out.println(client.getRemoteAddress().toString()+"---->>["+msg+"]");
                                    client.write(ByteBuffer.wrap(msg.getBytes()));
                                    atta.clear();
                                }
                                client.read(atta,atta,this);
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }

                        @Override
                        public void failed(Throwable exc, Object attachment) {
                            exc.printStackTrace();
                        }
                    });
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                exc.printStackTrace();
            }
        });

        while (true){
            Thread.sleep(300);
        }
    }
}
