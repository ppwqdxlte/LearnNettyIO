package com.tankNettyStudy.s2;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author:李罡毛
 * @date:2021/3/17 11:26
 * 图形化Client
 * 暴露调用接口 new Client().connect();
 */
public class Client extends Frame {

    private TextArea textArea;
    private TextField textField;

    public static void main(String[] args) {
        new Client();
    }

    public Client(){
        this.setSize(800,600);
        this.setLocation(200,100);
        textArea = new TextArea();
        textField = new TextField();
        this.add(textArea,BorderLayout.CENTER);
        this.add(textField,BorderLayout.SOUTH);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        this.setVisible(true);
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText(textArea.getText()+textField.getText());
                textField.setText("");
            }
        });
    }
}
