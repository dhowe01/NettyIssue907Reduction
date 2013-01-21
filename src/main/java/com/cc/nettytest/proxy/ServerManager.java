package com.cc.nettytest.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;



public class ServerManager {
	
	
	private List<ConnectionReflector> waitingClientConnections;
    private Map<String,ConnectionReflector> waitingHTTPConnections; //key=uri, value = reflector.

	private List<ConnectionReflector> establishedConnections;

	public ServerManager( ) {


		this.waitingClientConnections = Collections
				.synchronizedList(new ArrayList<ConnectionReflector>());
		this.waitingHTTPConnections = new ConcurrentHashMap<String,ConnectionReflector>();
		this.establishedConnections = Collections
				.synchronizedList(new ArrayList<ConnectionReflector>());


		
	}




    public void establishHTTPRequestChannel(ChannelHandlerContext ctx, String uri) {
        // 1st store the channel in our reflector object, and place in our waiting list.
        this.waitingHTTPConnections.put(uri, new ConnectionReflector(this, ctx.channel()));
        // 2nd request a HTTP stream for it
        //DONT NEED TO DO THIS FOR THE TEST.  requestHTTPDataStream(uri, portByType(httpType));
    }
	


    public void stop() {
        if (this.waitingClientConnections != null) {
            final Iterator<ConnectionReflector> its = this.waitingClientConnections.iterator();
            while (its.hasNext()) {
                final ConnectionReflector ref = its.next();
                its.remove();
                ref.stop();
            }
        }

        if (this.waitingHTTPConnections != null) {
            final Iterator<Map.Entry<String, ConnectionReflector>> its = this.waitingHTTPConnections
                .entrySet().iterator();
            while (its.hasNext()) {
                final Map.Entry<String, ConnectionReflector> ref = its.next();
                its.remove();
                ref.getValue().stop();
            }
        }

        if (this.establishedConnections != null) {
            final Iterator<ConnectionReflector> its = this.establishedConnections.iterator();
            while (its.hasNext()) {
                final ConnectionReflector ref = its.next();
                its.remove();
                ref.stop();
            }
        }

    }

	public final void removeConnection(final ConnectionReflector connectionReflector) {
		if (! this.waitingClientConnections.remove(connectionReflector) ){
		    if ( ! this.establishedConnections.remove(connectionReflector) ){
		        if ( this.waitingHTTPConnections != null && this.waitingHTTPConnections.size() > 0){
		            Iterator<Entry<String, ConnectionReflector>> entries = waitingHTTPConnections.entrySet().iterator();
		            Entry<String, ConnectionReflector> entry = null;
		            while (entries.hasNext()){
		                entry = entries.next();
		                if ( connectionReflector == entry.getValue()){
		                    entries.remove();
		                    break;
		                }
		            }
		         }
		    }
		}
		connectionReflector.stop();
	}



    public final boolean establishHTTPResponseChannel(final String uri, final Channel channel) {
        boolean retValue = false;
        // 1st get the object from our waiting bucket
        if (this.waitingHTTPConnections.containsKey(uri) ){
            final ConnectionReflector reflector = this.waitingHTTPConnections.remove(uri);
            //2nd start reflecting, and place the object in our running connections bucket
             reflector.startReflectingToUIManager(channel);
            this.establishedConnections.add(reflector);
            retValue = true;
        } else {
            System.out.println("No client is waiting for an incoming HTTP data connection ");
        }
        return retValue;
    }


}
