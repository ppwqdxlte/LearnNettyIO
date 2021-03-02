package com.lgm.BIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author:李罡毛
 * @date:2021/3/1 16:32
 * 由于accept()是个阻塞点，read()又是个阻塞点
 * 为了继续执行，简单模拟实现了【最早的BIO模型，每个连接对应一个线程】
 */
public class SocketIO {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(9090);
            System.out.println("step 1: new ServerSocket, port:9090");

            class ReadRunnable implements Runnable {
                Socket clientSocket;

                public ReadRunnable(Socket clientSocket) {
                    this.clientSocket = clientSocket;
                }

                @Override
                public void run() {

                    InputStream clientInputStream = null;
                    BufferedReader bufferedReader = null;
                    String readLine = "";
                    try {
                        while (!clientSocket.isClosed()) {//每一客户端无限输入
                            clientInputStream = clientSocket.getInputStream();
                            bufferedReader = new BufferedReader(new InputStreamReader(clientInputStream, StandardCharsets.UTF_8));
                            readLine = bufferedReader.readLine();
                            System.out.println(clientSocket.getPort() + "------->>>>>" + readLine);
                            if (readLine.contains("exit")) {
                                System.out.println("Client:"+clientSocket.getPort()+"  SHUT DOWN !!!!!");
                                if (bufferedReader != null) bufferedReader.close();
                                if (clientInputStream != null) clientInputStream.close();
                                if (clientSocket != null) clientSocket.close();
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            while (true){//轮询
                Socket client = serverSocket.accept();
                if (client != null){
                    System.out.println("setp 2: clien is connected,it's port is "+client.getPort());
                    ReadRunnable readRunnable = new ReadRunnable(client);
                    executorService.submit(readRunnable);
                }else{
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (serverSocket != null){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
