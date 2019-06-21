package tech.stdev.core2;

public interface SocketDataForwardingPolicy{
	
	void receive(byte[] payload);
}
