package com.cc.nettytest.proxy.controller;




import com.cc.nettytest.TestServer;
import com.cc.nettytest.proxy.ServerManager;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class ClientHTTPConnectionController {
	private final int genericHttpPort;

	private Channel genericHttpChannel     = null;

	private TestServer parent = null;

	public ClientHTTPConnectionController(final TestServer clareLinkServer) {
		this.genericHttpPort = 8081;
		this.parent = clareLinkServer;
	}
	
	public void serve() throws Exception {
	    
	    genericHttpChannel     = this.startServerOnPort( genericHttpPort);
	}
	
	
	
    private Channel startServerOnPort( int parmPort) throws InterruptedException {
        System.out.println("Starting  Client HTTP Listener on port {}..." + parmPort);
        final ServerBootstrap bstrap = new ServerBootstrap();

        bstrap.group(new NioEventLoopGroup(), parent.getSharedEventLoop()).channel(NioServerSocketChannel.class)
            .localAddress(parmPort).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(final SocketChannel channel) throws Exception {
                    final ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast("decoder", new HttpRequestDecoder());
                    pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
                    pipeline.addLast("encoder", new HttpResponseEncoder());
                    pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
                    pipeline.addLast("handler", new ClientHTTPConnectionHandler(ClientHTTPConnectionController.this));
                }
            });
        // Bind to the port and wait for start up
        // this should be an instance of NIOServerSocketChannel
        final Channel channel = bstrap.bind().sync().channel();
        System.out.println("Started ClareLink Client HTTP Communication on port {}" + parmPort);

        channel.closeFuture().addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture arg0) throws Exception {
                System.out.println("Shutting down ClareLink Client Communication");
                bstrap.shutdown();
                parent = null;
                System.out.println("Shutdown ClareLink Client Communication");
            }
        });
        return channel;
    }
	

    public void stop() {
        if (this.genericHttpChannel != null) {
            if (this.genericHttpChannel.isOpen()) {
                this.genericHttpChannel.close();
            }
        }

    }


    //public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    //public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";

    public boolean establishHTTPConnection( String uri, ChannelHandlerContext ctx) {
        boolean retVal = false;
        final ServerManager manager = this.parent.getServerManager();
        if (manager != null) {
            manager.establishHTTPRequestChannel( ctx, uri);
            
            
            retVal = true;
        } 
        return retVal;
    }

    /**
     * @return the genericHttpPort
     */
    protected int getGenericHttpPort() {
        return genericHttpPort;
    }
    

    
}
