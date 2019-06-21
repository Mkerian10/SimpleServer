package tech.stdev.core2;

import java.net.Socket;

public interface SocketFactory{

	AbstractSocket create(Socket s);
	
}
