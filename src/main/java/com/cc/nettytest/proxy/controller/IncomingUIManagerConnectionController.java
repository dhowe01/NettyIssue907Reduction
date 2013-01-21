package com.cc.nettytest.proxy.controller;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import com.cc.nettytest.TestServer;
import com.cc.nettytest.proxy.ServerManager;
import com.cc.nettytest.proxy.decoder.CCLengthFieldBasedFrameDecoder;
import com.cc.nettytest.proxy.decoder.JsonDecoder;
import com.cc.nettytest.proxy.encoder.CCLengthFieldPrepender;
import com.cc.nettytest.proxy.encoder.JsonEncoder;

public class IncomingUIManagerConnectionController {

	public static final int DEFAULT_UIMANAGER_LISTENER_PORT = 1776;
	private static final int SIZE_OF_INT = Integer.SIZE / 8; // 4 bytes

	private final int port;
	private Channel channel = null;
	private  TestServer parent = null;

	public IncomingUIManagerConnectionController(final TestServer clareLinkServer) {
		this.port = DEFAULT_UIMANAGER_LISTENER_PORT;
		this.parent = clareLinkServer;

	}

	public void serve() throws Exception {
	    System.out.println("Starting Server Communication on port {}..." + this.port);
		final ServerBootstrap bstrap = new ServerBootstrap();
		bstrap.group(new NioEventLoopGroup(),  parent.getSharedEventLoop())
			.channel(NioServerSocketChannel.class).localAddress(this.port)
			.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(final SocketChannel channel) throws Exception {
					final ChannelPipeline pipeline = channel.pipeline();
					pipeline.addLast("lengthDecoder",
								new CCLengthFieldBasedFrameDecoder(
										Integer.MAX_VALUE, 0, SIZE_OF_INT, 0,
										SIZE_OF_INT));
					pipeline.addLast("lengthEncoder", new CCLengthFieldPrepender(SIZE_OF_INT));
					pipeline.addLast("jsonDecoder", new JsonDecoder());
					pipeline.addLast("jsonEncoder", new JsonEncoder());
					pipeline.addLast("equatorUIManagerhandler",
							new IncomingUIManagerConnectionHandler(
									IncomingUIManagerConnectionController.this));
				}
			});
		// Bind to the port and wait for start up this should be an instance of NIOServerSocketChannel
		this.channel = bstrap.bind().sync().channel();
		System.out.println("Started Server Communication on port {}" + this.port);
		this.channel.closeFuture().addListener(new ChannelFutureListener() {
			public void operationComplete(final ChannelFuture future) throws Exception {
			    System.out.println("Shutting down Server Communication");
				bstrap.shutdown();
				channel = null;
				parent = null;
				System.out.println("Shutdown Server Communication");
			}
		});
	}

	public void stop() {
		if (this.channel != null) {
			if (this.channel.isOpen()) {
				this.channel.close();
			}
		}
	}

	
	
	   public boolean establishHTTPResponseConnection(String uri, final Channel channel) {
	        boolean retVal = false;
	        final ServerManager manager = this.parent.getServerManager();
	        if (manager != null) {
	            retVal = manager.establishHTTPResponseChannel(uri, channel);
	        } 
	        return retVal;
	    }
}
