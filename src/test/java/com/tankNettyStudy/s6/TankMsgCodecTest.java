package com.tankNettyStudy.s6;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 1、实际生产中责任链中的listener众多，每次调试都要重启Server太麻烦，
 *      故而Netty提供了一个Embedded嵌入式的Channel，假Channel测试是否通过；
 * 2、Junit+EmbeddedChannel，双向测试；
 * 【Junit好处】，最重要就是可以复用测试
 *
 */
class TankMsgCodecTest {

    @Test
    void encode() {
        TankMsg tankMsg = new TankMsg(300,200);
        EmbeddedChannel channel = new EmbeddedChannel(new TankMsgEncoder());
        channel.writeOutbound(tankMsg);

        ByteBuf byteBuf = (ByteBuf) channel.readOutbound();
        Assertions.assertTrue(byteBuf.readInt() == 300 && byteBuf.readInt() == 200);
        byteBuf.release();
    }

    @Test
    void decode() {
        ByteBuf byteBuf = Unpooled.buffer();
        TankMsg tankMSg = new TankMsg(300,200);
        byteBuf.writeInt(tankMSg.getX());
        byteBuf.writeInt(tankMSg.getY());

        EmbeddedChannel channel = new EmbeddedChannel(new TankMsgEncoder(),new TankMsgDecoder());
        channel.writeInbound(byteBuf.duplicate());

        TankMsg msg = (TankMsg) channel.readInbound();
        Assertions.assertTrue(msg.getX() == tankMSg.getX() && msg.getY() == tankMSg.getY());

    }
}