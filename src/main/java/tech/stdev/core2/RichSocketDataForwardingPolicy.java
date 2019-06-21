package tech.stdev.core2;

public interface RichSocketDataForwardingPolicy extends SocketDataForwardingPolicy{
	
	void receive(String string);
	
	void receive(Object deserialized);
	
	//Method for testing, gets called every time
	default void receive(byte opcode, byte[] payload){
	
	}
	
}
