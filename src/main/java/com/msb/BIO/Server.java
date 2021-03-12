package com.msb.BIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author:李罡毛
 * @date:2021/3/11 16:55
 */
public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket();
        ss.bind(new InetSocketAddress("127.0.0.1",8888));

        while (true){
            Socket s = ss.accept();
            new Thread(()->handle(s)).start();
        }
    }
    private static void handle(Socket s) {
        System.out.println("客户端"+s.getPort()+"链接进来了");
        try{
            byte[] bytes = new byte[1024];
            int len = s.getInputStream().read(bytes);
            if (len < 0) {
                System.out.println(s.getPort()+"客户端关闭！");
                s.close();
                return;
            }
            System.out.println(new String(bytes,0,len));

            s.getOutputStream().write(bytes,0,len);
            s.getOutputStream().flush();
        }catch (IOException ioException){
            ioException.printStackTrace();
        }
    }
}
