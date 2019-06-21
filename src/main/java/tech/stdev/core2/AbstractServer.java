package tech.stdev.core2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

public abstract class AbstractServer implements Runnable{
	
	public AbstractServer(ServerSocket serverSocket){
		this.serverSocket = serverSocket;
	}
	
	private final ServerSocket serverSocket;
	
	@Override
	public void run(){
		Log.log(Level.INFO, "Server up and running! Listening on " + serverSocket.getInetAddress().getHostName() + ":" + serverSocket.getLocalPort() + ".");
		
		while(!serverSocket.isClosed()){
			try{
				Socket socket = serverSocket.accept();
				Log.log(Level.INFO, "New Socket accepted from " + socket.getInetAddress().getHostAddress() + ".");
				onNewSocket(socket);
			}catch(IOException e){
				Log.log(Level.INFO, "Server shut down while listening for sockets.");
			}
		}
	}
	
	protected abstract void onNewSocket(Socket socket);
	
	public void shutdown(){
		try{
			serverSocket.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
