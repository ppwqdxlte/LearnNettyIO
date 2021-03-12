package com.msb;
/**
 *
 * 复习BIO-NIO-AIO-Netty编程模型
 *            i.网络程序的烦点：异常处理 正确关闭→线程的正常结束→线程池的正常结束
 *            ii.整个BIO就是半双工，读写不能同时进行，且关一边的流，反向流也用不了，
 * inputStream\outputStream 要开就同时开着，要关就一起关了
 *            iii.AIO异步，不管有没有Event都继续往下执行，不阻塞，Windows系统上AIO效率优于NIO，
 *            因为AIO用到了CompletionPort，而在Linux上AIO只是对NIO的封装，背后用的还是轮询（selector.select(...)）
 *
 *  【问题】 JDK的NIO里面的ByteBuffer 和  Netty里面的ByteBuf有啥区别？
 *         前者只有一个指针，后者两个指针，一个读指针，一个写指针
 *
 *
 *
 */