package com.tankNettyStudy.s6;

/**
 * @author:李罡毛
 * @date:2021/3/22 20:45
 */
public class TankMsg {
    private int x,y;
    public TankMsg(int x,int y){
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "TankMsg:{x="+x+",y="+y+"}";
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
