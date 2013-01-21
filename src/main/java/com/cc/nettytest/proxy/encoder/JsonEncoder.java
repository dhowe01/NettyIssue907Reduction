package com.cc.nettytest.proxy.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.json.JSONObject;

public class JsonEncoder extends MessageToMessageEncoder<JSONObject> {
	
	@Override
	public ByteBuf encode(final ChannelHandlerContext ctx, final JSONObject msg)
	throws Exception {
		return Unpooled.wrappedBuffer(msg.toString().concat("\n").getBytes("UTF-8"));
	}
}
