package com.cc.nettytest.proxy.controller;

import java.util.List;
import java.util.Map;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;


public class ClientHTTPConnectionHandler extends ChannelInboundMessageHandlerAdapter<HttpRequest> {
	

	private ClientHTTPConnectionController controller;

	public ClientHTTPConnectionHandler(final ClientHTTPConnectionController owningController ) {
		this.controller = owningController;
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final HttpRequest request) throws Exception {
		if (request != null) {
		    System.out.println("Received a http request" + request.toString());
	        if (!request.decoderResult().isSuccess()) {
	            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
	            return;
	        }

	        if (request.method() != HttpMethod.GET) {
	            sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
	            return;
	        }

	        String uri = request.uri();
	        if (uri == null) {
	            sendError(ctx, HttpResponseStatus.FORBIDDEN);
	            return;
	        }
	        
	        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
	        Map<String,List<String>> parms = decoder.parameters();
	        if ( parms != null){
	                evaluateRequest( uri, ctx) ;

	            }else{
	                System.out.println("Received a HTTP request without a serverid parm: " +request.uri());
	                sendError(ctx, HttpResponseStatus.BAD_REQUEST);
	                return;
	            }
	        }
		}


	        
	        
	        private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
	            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
	            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
	            response.data().writeBytes(Unpooled.copiedBuffer(
	                    "Failure: " + status.toString() + "\r\n",
	                    CharsetUtil.UTF_8));

	            // Close the connection as soon as the error message is sent.
	            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
	        }

	        
	        
	private void evaluateRequest(final String uri,
			final ChannelHandlerContext ctx) {
	    System.out.println("Accepted a new HTTP request for  Server: " + " from remote address "
				+ ctx.channel().remoteAddress());

		if (this.controller.establishHTTPConnection( uri,ctx)) {


		    ctx.channel().flush();
		    

			// then remove everything from the Incoming UIManager Connection Handler:
			final ChannelPipeline pipeline = ctx.channel().pipeline();

			pipeline.remove("decoder");
			pipeline.remove("aggregator");
			pipeline.remove("encoder");
			pipeline.remove("chunkedWriter");
			pipeline.remove(this);
			

			
 		} else {
			// Couldn't establish a connection so, bail out.
			if (ctx.channel().isActive()) {
			    ctx.channel().flush().addListener(ChannelFutureListener.CLOSE);
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
