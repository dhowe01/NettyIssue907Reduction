package com.cc.nettytest.proxy.controller;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundByteHandlerAdapter;



/**
 * Handles a server-side channel.
 */
public class DiscardHandler extends ChannelInboundByteHandlerAdapter {



    @Override
    public void inboundBufferUpdated(ChannelHandlerContext ctx, ByteBuf in)
            throws Exception {
        // Discard the received data silently.
        in.clear();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
            Throwable cause) throws Exception {
        // Close the connection when an exception is raised.
       cause.printStackTrace();
        ctx.close();
    }
}