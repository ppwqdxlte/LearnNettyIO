package com.tankNettyStudy.s4;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author:李罡毛
 * @date:2021/3/18 21:34
 */
public class ServerFrame extends Frame {

    public static final ServerFrame INSTANCE = new ServerFrame();

    private Server server;

    private ServerFrame(){
        this.setSize(1000,800);
        this.setLocation(200,100);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                server.shutDown();
                System.exit(0);
            }
        });
        this.setVisible(true);
    }

    public void start(){
        this.server = new Server();
        this.server.start();
    }

    public static void main(String[] args) {
        ServerFrame.INSTANCE.start();
    }
}
