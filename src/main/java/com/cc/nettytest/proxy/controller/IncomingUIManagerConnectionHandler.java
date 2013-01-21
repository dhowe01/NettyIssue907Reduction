package com.cc.nettytest.proxy.controller;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import org.json.*;


public class IncomingUIManagerConnectionHandler extends ChannelInboundMessageHandlerAdapter<JSONObject> {

    private static final String MESSAGE_TYPE = "messageType";

    private IncomingUIManagerConnectionController controller;

    public IncomingUIManagerConnectionHandler(final IncomingUIManagerConnectionController owningController) {
        this.controller = owningController;
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final JSONObject msg) throws Exception {
        if (msg != null) {
            System.out.println("Received a msg" + msg.toString());
            final String incomingMessageType = msg.has(MESSAGE_TYPE) ? msg.getString(MESSAGE_TYPE) : null;
            if (incomingMessageType != null) {
                final String uri = msg.has("uri") ? msg.getString("uri") : null;
                this.evaluateNewHTTPResponseConnection(uri, ctx.channel());

            } else {
                System.out.println("Received a null message type");
            }
        } else {
            System.out.println("Received a null message");
        }
    }

    private void evaluateNewHTTPResponseConnection( final String uri, final Channel channel) {
        System.out.println("Accepted a new HTTP Response connection from  Server: " + uri);
        // This next line finds a waiting frontend channel and adds a handler to both 
        // the backend and frontend pipelines that begin to proxy the data between the two channels
        if (this.controller.establishHTTPResponseConnection( uri, channel)) {
            // We now want to remove exisiting handlers from the pipeline and 
            // directly proxy raw data back and forth

            final ChannelPipeline pipeline = channel.pipeline();
            // Save a refernce to the existing bytes, lest they disappear
            final ByteBuf oldIn = pipeline.inboundByteBuffer();
            // Removing this handler chucks any existing bytes in the pipeline
            // which is, um, unexpected
            pipeline.remove("lengthDecoder");
            pipeline.remove("lengthEncoder");
            pipeline.remove("jsonDecoder");
            pipeline.remove("jsonEncoder");
            pipeline.remove(this);

            // Add the existing bytes back into the pipeline so they will be
            // visible to the new proxy handler and eventually be written out
            pipeline.inboundByteBuffer().writeBytes(oldIn);
            pipeline.fireInboundBufferUpdated();
            
        } else {
            // Couldn't establish a connection so, bail out.
            if (channel.isActive()) {
                channel.flush().addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx != null) {
            ctx.close();
        }
    }
}
