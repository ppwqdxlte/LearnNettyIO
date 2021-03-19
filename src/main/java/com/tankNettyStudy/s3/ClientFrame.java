package com.tankNettyStudy.s3;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author:李罡毛
 * @date:2021/3/17 14:14
 * ClientFrame可以持有Client的引用，
 * 问题来了，TextField输入内容怎么发送给服务器呢？
 * 1.首先获取Client的Channel对象，要通过channel才能写给Server
 * 2.而ClientFrame要想把用户输入发送给Channel，需要Client暴露"接口"给ClientFrame
 * 3.单粒化ClientFrame
 */
public class ClientFrame extends Frame {

    public static final ClientFrame INSTANCE = new ClientFrame();

    private volatile TextArea textArea = new TextArea();
    private volatile TextField textField = new TextField();

    private Client client;

    private ClientFrame(){
        this.setSize(800,600);
        this.setLocation(200,100);
        this.setVisible(true);
        this.add(textArea,BorderLayout.CENTER);
        this.add(textField,BorderLayout.SOUTH);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.shutDown();
                System.exit(0);
            }
        });
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.sentTextToServer(textField.getText());
                textArea.setText(textArea.getText()+textField.getText());
                textField.setText("");
            }
        });
    }

    public void start(){
        this.client = new Client();
        client.connect();
    }

    public static void main(String[] args) {
        ClientFrame.INSTANCE.start();
    }

    public void displayInTextArea(byte[] bytes) {
        textArea.setText(textArea.getText()+System.lineSeparator()+new String(bytes));
    }
}
