package com.tankNettyStudy.s6;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author:李罡毛
 * @date:2021/3/22 21:02
 * 1、Codec之 Encoder和Decoder，转化TankMsg，或者其它Msg；
 * 2、客户端登录，坦克信息要发送给Server，群发出去，不会写的话看看Netty官网示例文档；
 * 3、实际生产中责任链中的listener众多，每次调试都要重启Server太麻烦，故而Netty提供了一个Embedded嵌入式的Channel，假Channel测试是否通过；
 * 4、Junit+EmbeddedChannel，双向测试；
 * 【Junit好处】，最重要就是可以复用测试
 * Gradle项目默认给test 目录，直接在里面测试就行；
 */
public class TankMsgDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < 8) return;//涉及 拆包粘包
//        if (!byteBuf.readCharSequence(7,StandardCharsets.UTF_8).toString().contains("TankMsg")) return;
        list.add(new TankMsg(byteBuf.readInt(),byteBuf.readInt()));
    }
}
