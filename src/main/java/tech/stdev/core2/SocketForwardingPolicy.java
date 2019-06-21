package tech.stdev.core2;

import java.net.Socket;

public interface SocketForwardingPolicy{
	
	void onNewSocket(Socket socket);
}
