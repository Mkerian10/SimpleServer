package tech.stdev.core2;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class RichSocketBuilder{
	
	private RichSocketBuilder(Socket socket){
		this.socket = socket;
	}
	
	public static RichSocketBuilder start(String host, int port){
		try{
			return start(new Socket(host, port));
		}catch(IOException e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static RichSocketBuilder start(InetAddress inetAddress, int port){
		try{
			return start(new Socket(inetAddress, port));
		}catch(IOException e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static RichSocketBuilder start(Socket socket){
		return new RichSocketBuilder(socket);
	}
	
	public static RichSocketBuilder start(int port){
		return start(InetAddress.getLoopbackAddress(), port);
	}
	
	private Socket socket;

	private RichSocketDataForwardingPolicy forwardingPolicy = null;
	
	private ScheduledExecutorService service;
	
	private int lifeCycle = -1;
	
	private int keepAliveTime = -1;
	
	private int pingTime = -1;
	
	public RichSocketBuilder setForwardingPolicy(RichSocketDataForwardingPolicy forwardingPolicy){
		this.forwardingPolicy = forwardingPolicy;
		return this;
	}
	
	public RichSocketBuilder setScheduledExecutor(ScheduledExecutorService service){
		this.service = service;
		return this;
	}
	
	public void setLifeCycle(int lifeCycle){
		this.lifeCycle = lifeCycle;
	}
	
	public void setKeepAliveTime(int keepAliveTime){
		this.keepAliveTime = keepAliveTime;
	}
	
	public void setPingTime(int pingTime){
		this.pingTime = pingTime;
	}
	
	public RichSocket build(){
		ScheduledExecutorService service = this.service == null ? Executors.newSingleThreadScheduledExecutor() : this.service;
		RichSocketDataForwardingPolicy policy = this.forwardingPolicy != null ? this.forwardingPolicy: new RichSocketDataForwardingPolicy(){
			@Override
			public void receive(String string){
			
			}
			
			@Override
			public void receive(Object deserialized){
			
			}
			
			@Override
			public void receive(byte[] payload){
			
			}
		};
		
		RichSocket richSocket = new RichSocket(socket, policy, service);
		if(lifeCycle != -1){
			richSocket.setSocketLifeCycleTime(lifeCycle);
		}
		
		if(keepAliveTime != -1){
			richSocket.setKeepAliveTime(keepAliveTime);
		}
		
		if(pingTime != -1){
			richSocket.setPingTime(pingTime);
		}
		
		return richSocket;
	}
}
