package com.cc.nettytest.proxy;

import java.util.Arrays;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundByteHandlerAdapter;


public class ProxyHandler extends ChannelInboundByteHandlerAdapter {


	private ConnectionReflector reflector;
	private Channel proxiedChannel;
	private long total = 0;

	public ProxyHandler(final ConnectionReflector reflector, final Channel proxiedChannel) {
		this.reflector = reflector;
		this.proxiedChannel = proxiedChannel;
	}

	@Override
	public void inboundBufferUpdated(final ChannelHandlerContext ctx, final ByteBuf inBuffer) throws Exception {

		final ByteBuf out = this.proxiedChannel.outboundByteBuffer();
		out.writeBytes(inBuffer);
		if (this.proxiedChannel.isActive()) {

			this.proxiedChannel.flush();
		}
	}

	@Override
	public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
		if (this.proxiedChannel != null) {
			closeOnFlush(this.proxiedChannel);
		}
	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
	    cause.printStackTrace();
		closeOnFlush(ctx.channel());
	}



	/**
	 * Closes the specified channel after all queued write requests are flushed.
	 */
	private void closeOnFlush(final Channel ch) {

		if (ch.isActive()) {
            ch.pipeline().firstContext().flush();
		    ch.flush().addListener( new ChannelFutureListener(){

                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if ( ch.isOpen() && ch.isActive()){
                        future.channel().close();
                    }
                    if (ProxyHandler.this.reflector != null) {
                        ProxyHandler.this.reflector.stop();
                    }
                }
		        
		    });//ChannelFutureListener.CLOSE);
		}
	}
}
