package com.cc.nettytest.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;


import com.cc.nettytest.proxy.controller.DiscardHandler;

public class ConnectionReflector {

    private Channel clientConnection;
    private Channel uiManagerConnection;
    private ServerManager parent;

    ConnectionReflector(final ServerManager serverManager, final Channel clientConnection) {
        this.parent = serverManager;
        this.clientConnection = clientConnection;

        /*
         * Add a listener to this channel to stop reflecting if the connection
         * drops.
         */
        // listenForChannelFailure(clientConnection);

        /*
         * add a placeholder handler to consume any bytes that might come
         * through and to trap any errors that might happen before connection to
         * equator server can be established.
         */
        this.clientConnection.pipeline().addLast("byteConsumer", new DiscardHandler());
    }

    public void startReflectingToUIManager(final Channel parmUIManagerChannel) {
        this.uiManagerConnection = parmUIManagerChannel;

        if (uiManagerConnection != null && this.clientConnection != null) {

            // This causes problems. bad bad bad
            /* make the two connections use the same event loop */
            final EventLoop sharedLoop = this.clientConnection.eventLoop().parent().next();

            this.clientConnection.deregister().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    sharedLoop.register(future.channel()).sync();
                }
            });
            this.uiManagerConnection.deregister().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    sharedLoop.register(future.channel()).sync();
                }
            });

            /* give each connection a proxy handler */

            this.clientConnection.pipeline().addLast("proxyHandler", new ProxyHandler(this, this.uiManagerConnection));
            this.uiManagerConnection.pipeline().addLast("proxyHandler", new ProxyHandler(this, this.clientConnection));

            // now remove the discard handler
            this.clientConnection.pipeline().remove("byteConsumer");

        }
    }

    private boolean stopping = false;

    public final void stop() {

        if (stopping) {
            return;
        } else {
            stopping = true;

            if (this.parent != null) {
                final ServerManager _parent = this.parent;
                this.parent = null;
                _parent.removeConnection(this);
            }
            if (this.clientConnection != null) {
                if (this.clientConnection.isOpen()) {
                    if (this.clientConnection.isActive()) {
                        this.clientConnection.flush().addListener(ChannelFutureListener.CLOSE);
                    } else {
                        this.clientConnection.close();
                    }
                }
                this.clientConnection = null;
            }
            if (this.uiManagerConnection != null) {
                if (this.uiManagerConnection.isOpen()) {
                    if (this.uiManagerConnection.isActive()) {
                        this.uiManagerConnection.flush().addListener(ChannelFutureListener.CLOSE);
                    } else {
                        this.uiManagerConnection.close();
                    }
                }
                this.uiManagerConnection = null;
            }
        }
    }

}
