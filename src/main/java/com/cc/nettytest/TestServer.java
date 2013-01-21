package com.cc.nettytest;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioEventLoopGroup;

import com.cc.nettytest.proxy.ServerManager;
import com.cc.nettytest.proxy.controller.ClientHTTPConnectionController;
import com.cc.nettytest.proxy.controller.IncomingUIManagerConnectionController;

public class TestServer {

	private IncomingUIManagerConnectionController uiManagerController = null;
	private ClientHTTPConnectionController httpController = null;
	private ServerManager serverManager = null;
	EventLoopGroup group;
	EventLoop loop;
	EventLoop shared = null;

	public void serve() throws Exception {
		System.out.println("Starting Proxy ");

		group = new NioEventLoopGroup();
		loop = group.next();
		
		this.createServerManager( );
		

		this.uiManagerController = new IncomingUIManagerConnectionController(this);
		this.uiManagerController.serve();

		
        this.httpController = new ClientHTTPConnectionController(this);
        this.httpController.serve();		
	}
	
	public EventLoopGroup getSharedEventLoopGroup(){
	    return group;
	}
	   public EventLoop getSharedEventLoop(){
	        return loop;
	    }


	public void stop() {

		if (this.uiManagerController != null) {
			this.uiManagerController.stop();
			this.uiManagerController = null;
		}

	
        if (this.httpController != null) {
            this.httpController.stop();
            this.httpController = null;
        }
        if ( serverManager != null){
	    serverManager.stop();
		}
        System.out.println("Proxy Stopped");
	}

	public void createServerManager() {
		removeServerManager();
		serverManager = new ServerManager();
	}



	public final void removeServerManager() {
		if (this.serverManager != null) {
		    serverManager.stop();
		    serverManager = null;
		}
	}

	public ServerManager getServerManager() {
		return serverManager;
	}
	
	public static void main(String[] args){
	    
	    //Start the server
	    TestServer server = new TestServer();
	    

	    
	    try {
            server.serve();
            
            //Open a socket to make an http request
                HttpURLConnection connection = (HttpURLConnection)new URL("http://localhost:8081/test1.html").openConnection() ;
                connection.setUseCaches(false);
                connection.setDoOutput(true);
                connection.setRequestMethod("GET");

                //Get Response    
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer(); 
                while((line = rd.readLine()) != null) {
                  response.append(line);
                  response.append('\r');
                }
                
                
                

        } catch (Exception e) {
            if ( server != null){
                server.stop();
                e.printStackTrace();
                System.exit(1);
            }
        }
	}
}
