package tech.stdev.core2;

import java.io.IOException;
import java.net.Socket;

public abstract class AbstractSocket implements Runnable{
	
	AbstractSocket(Socket socket){
		this.socket = socket;
	}
	
	protected final Socket socket;
	
	public abstract boolean send(byte[] bytes);
	
	protected abstract void read();
	
	public abstract void shutdown();
}
