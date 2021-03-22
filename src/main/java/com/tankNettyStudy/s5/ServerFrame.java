package com.tankNettyStudy.s5;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author:李罡毛
 * @date:2021/3/22 9:44
 */
public class ServerFrame extends Frame {

    public static final ServerFrame INSTANCE = new ServerFrame();
    private TextArea taLeft;
    private TextArea taRight;
    private Server server;

    private ServerFrame(){
        this.setSize(800,600);
        this.setLocation(300,200);
        taLeft = new TextArea(10,1);
        taRight = new TextArea(10,2);
        Panel panel = new Panel(new GridLayout(1,2));
        panel.add(taLeft);panel.add(taRight);
        this.add(panel);
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

    public void updateTaRight(byte[] bytes) {
        taRight.setText(taRight.getText()+System.lineSeparator()+new String(bytes));
    }

    public void updateTaLeft(byte[] bytes){
        taLeft.setText(taLeft.getText()+System.lineSeparator()+new String(bytes));
    }

    public static void main(String[] args) {
        ServerFrame.INSTANCE.start();
    }

}
