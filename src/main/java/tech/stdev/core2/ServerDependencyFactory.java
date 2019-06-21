package tech.stdev.core2;


import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerDependencyFactory{
	
	public final static int DEFAULT_PORT = 41368;
	
	public final static int DEFAULT_BACKLOG = 50;
	
	public static ServerSocket localServerSocket(){
		try{
			return new ServerSocket(DEFAULT_PORT, DEFAULT_BACKLOG, InetAddress.getLoopbackAddress());
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static Socket localSocket(){
		try{
			return new Socket("localhost", DEFAULT_PORT);
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}
	}
}
