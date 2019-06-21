package tech.stdev.core2;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Await is a simple system used to verify any sort of input. It's used slightly more in internal
 * situations, if attempting to read data for outside reasons then using a SocketDataFormattingPolicy
 * is preferred. Awaits are for simple examples, and can potentially be used for specific triggers.
 *
 * An example of await being used can be found in the shutdown method of RichSocket. It'll try twice
 * to inform the other side of a shutdown, on the first one it'll wait to see if the other side response
 * so it can ensure both sides are aware of the shutdown. However after the second one it'll shut
 * down after giving time for a response.
 */
public interface Await{
	
	boolean await(byte[] payload, byte opcode);
	
	void schedule(ScheduledExecutorService service);
}
