package com.cc.nettytest.proxy.decoder;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToMessageDecoder;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    public JSONObject decode(final ChannelHandlerContext ctx, final ByteBuf msg) throws CorruptedFrameException {
        final byte[] byteArr;
        if (msg.hasArray()) {
            if (msg.arrayOffset() == 0 && msg.readableBytes() == msg.capacity()) {
                // We have no offset and the length is the same as the capacity.
                // Its safe to reuse the array without copy it first
                byteArr = msg.array();
            } else {
                // Copy the ChannelBuffer to a byte array
                byteArr = new byte[msg.readableBytes()];
                // msg.getBytes(0, byteArr)
                msg.readBytes(byteArr, 0, msg.readableBytes());
            }
        } else {
            // Copy the ChannelBuffer to a byte array
            byteArr = new byte[msg.readableBytes()];
            // msg.getBytes(0, byteArr);
            msg.readBytes(byteArr, 0, msg.readableBytes());

        }
        JSONObject result = null;
        if (byteArr.length > 0) {
            final String jsonString = new String(byteArr);
            try {
                result = new JSONObject(jsonString);
            } catch (JSONException e) {
                throw new CorruptedFrameException("Error deserializing json " + new String(byteArr));
            }
        }
        if (result == null) {
            throw new CorruptedFrameException("Error deserializing json");
        }
        return result;
    }
}
