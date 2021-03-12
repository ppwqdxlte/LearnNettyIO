package com.msb.BIO;

import java.io.IOException;
import java.net.*;

/**
 * @author:李罡毛
 * @date:2021/3/11 16:14
 */
public class Client {
    public static void main(String[] args) throws IOException, InterruptedException {
        Socket s = new Socket("127.0.0.1",8888);
        s.getOutputStream().write("Hello server,I'm client".getBytes());
        System.out.println("Write over,wait for msg back!");
        s.getOutputStream().flush();Thread.sleep(3000);
        s.getOutputStream().write("...........".getBytes());s.getOutputStream().flush();
        //s.getOutputStream().close(); 不要在InputStream结束前关闭，否则会报错
        byte[] bytes = new byte[1024];
        int read = s.getInputStream().read(bytes);
        System.out.println(new String(bytes));
        //关闭接头
        s.close();
    }
}
