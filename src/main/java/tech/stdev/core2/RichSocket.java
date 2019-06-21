package tech.stdev.core2;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * RichSocket is a direct descendent of ConcurrentSocket.
 * <p>
 * This socket supports direct serialization and deserialization of strings and objects. In addition
 * to that this socket supports various other modifiers not found in other sockets just as keep alive
 * and multiple forms of concurrency.
 * <p>
 * This is the most featured socket in the entire API, and is the only one that should be seriously used
 * outside of directly extending an abstract socket.
 */
public class RichSocket extends AbstractSocket{
	
	//Opcodes for sending/receiving messsages
	private final static byte OPCODE_NONE = 0b0;
	private final static byte OPCODE_STRING = 0b1;
	private final static byte OPCODE_STRING_ENC = 0b10;
	private final static byte OPCODE_OBJECT = 0b11;
	final static byte OPCODE_PING = 0b100;
	final static byte OPCODE_PONG = 0b101;
	final static byte OPCODE_KILL = 0b110;
	final static byte OPCODE_KILL_RET = 0b111;
	
	/**
	 * Maximum amount of time (in MS) allotted for the foreign socket to confirm a shutdown.
	 */
	public static int SHUTDOWN_TIME = 5000;
	
	/**
	 * Maximum amount of time (in MS) to receive a pong after pinging
	 */
	public static int PONG_ACCEPTANCE_TIME = 8000;
	
	/**
	 * Await ran when shutdown is called. This waits for a SHUTDOWN_ACK in order to ensure both
	 * sides gracefully shut down. It'll try twice to shut down, and if no SHUTDOWN_ACK is heard
	 * it'll shutdown the socket regardless.
	 */
	private final Await SHUTDOWN_AWAIT = new Await(){
		@Override
		public boolean await(byte[] payload, byte opcode){
			return opcode == OPCODE_KILL_RET;
		}
		
		@Override
		public void schedule(ScheduledExecutorService service){
			try{
				service.schedule(() -> {
					if(RichSocket.this.running){
						sendShutdown();
						try{
							Thread.sleep(SHUTDOWN_TIME / 2);
						}catch(InterruptedException ignored){
						}
						kill();
					}
				}, SHUTDOWN_TIME / 2, TimeUnit.MILLISECONDS);
			}catch(RejectedExecutionException e){
				Log.log(Level.WARNING, "Shutdown await execution terminated early.");
			}
		}
	};
	
	private final Await PONG_AWAIT = new Await(){
		@Override
		public boolean await(byte[] payload, byte opcode){
			return opcode == OPCODE_PONG;
		}
		
		@Override
		public void schedule(ScheduledExecutorService service){
			try{
				service.schedule(() -> {
					if(overAssurance()){
						System.out.println("Over assurance");
						sendPing(true);
						try{
							Thread.sleep(PONG_ACCEPTANCE_TIME / 2);
						}catch(InterruptedException e){
							e.printStackTrace();
						}
						if(overAssurance()){
							kill();
						}
					}
				}, PONG_ACCEPTANCE_TIME / 2, TimeUnit.MILLISECONDS);
			}catch(RejectedExecutionException e){
				Log.log(Level.WARNING, "Delayed execution of PONG_AWAIT never ran.");
			}
		}
	};
	
	/**
	 * Runnable that checks the three possible methods of timeout (assurance, substance, and life-cycle)
	 * and appropriately responds if any of them are
	 */
	private final Runnable ENSURE_CONN = () -> {
		if(overLifeCycleTime() || overSubstance()){
			shutdown();
			return;
		}
		
		if(overAssurance()){
			sendPing();
		}
	};
	
	public RichSocket(Socket socket, RichSocketDataForwardingPolicy policy, ScheduledExecutorService executor){
		super(socket);
		this.policy = policy;
		this.executor = executor;
		executor.scheduleWithFixedDelay(ENSURE_CONN, pingTime, 1000, TimeUnit.MILLISECONDS);
	}
	
	
	/**
	 * Maximum time the connection may be established, regardless of when the last assurance was.
	 *
	 * -1 means the socket will never shut off due to this.
	 */
	private int socketLifeCycleTime = -1;
	
	/**
	 * Maximum time after previous message of substance (from either end) before a shut down is initialized
	 */
	private int keepAliveTime = 10000;
	
	/**
	 * Maximum time after previous assurance before a ping is sent to the foreign server.
	 */
	private int pingTime = 3000;
	
	/**
	 * The time when the socket was created
	 */
	private long creationTime = System.currentTimeMillis();
	
	/**
	 * Tracks time since lasts assurance received. Assurances are any message including ping/pong.
	 */
	private long lastAssurance = System.currentTimeMillis();
	
	/**
	 * Tracks time since last substantial message received/sent. Substantial messages are any non-ping/pongs.
	 */
	private long lastSubstance = System.currentTimeMillis();
	
	private final RichSocketDataForwardingPolicy policy;
	
	private boolean running = true;
	
	private final ScheduledExecutorService executor;
	
	private final List<Await> awaits = new ArrayList<>();
	
	public boolean packAndSend(byte opcode, byte[] payload){
		if(!isRunning()){
			return false; //False if can't be sent
		}
		
		int size = payload.length; //1 + for opcode
		byte[] sizeBytes = Utils.intToBytes(size);
		
		ByteBuffer bb = ByteBuffer.allocate(size + 5); // + 5 for the actual size param itself and the opcode
		
		bb.put(sizeBytes); //Content size
		bb.put(opcode);
		bb.put(payload);
		
		try{
			socket.getOutputStream().write(bb.array());
		}catch(IOException e){
			//should never execute
			Log.log(Level.SEVERE, "Socket unexpectedly closed!");
			e.printStackTrace();
		}
		if(opcode != OPCODE_PING && opcode != OPCODE_PONG){
			lastSubstance = System.currentTimeMillis();
		}
		
		return true;
	}
	
	@Override
	public boolean send(byte[] bytes){
		return packAndSend(OPCODE_NONE, bytes);
	}
	
	public boolean send(String string){
		return packAndSend(OPCODE_STRING, string.getBytes());
	}
	
	public void send(Serializable serializable){
		try{
			var baos = new ByteArrayOutputStream();
			var oos = new ObjectOutputStream(baos);
			oos.writeObject(serializable);
			packAndSend(OPCODE_OBJECT, baos.toByteArray());
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void sendPing(){
		sendPing(false);
	}
	
	private void sendPing(boolean lastTry){
		byte[] bytes = new byte[8];
		packAndSend(OPCODE_PING, bytes);
		if(!lastTry){
			await(PONG_AWAIT);
		}
	}
	
	public void sendPong(byte[] payload){
		packAndSend(OPCODE_PONG, payload);
	}
	
	public void sendShutdown(){
		byte[] bytes = new byte[8];
		packAndSend(OPCODE_KILL, bytes);
		SHUTDOWN_AWAIT.schedule(executor);
	}
	
	public void confirmShutdown(){
		byte[] bytes = new byte[8];
		packAndSend(OPCODE_KILL_RET, bytes);
		kill();
	}
	
	public void await(Await await){
		await.schedule(executor);
		awaits.add(await);
	}
	
	@Override
	public void run(){
		Log.log(Level.INFO, "Socket connection opened, listening to " + socket.getInetAddress().getHostName() + ".");
		
		while(running){
			read();
		}
	}
	
	private boolean overLifeCycleTime(){
		return socketLifeCycleTime != -1 && System.currentTimeMillis() - socketLifeCycleTime > creationTime;
	}
	
	private boolean overAssurance(){
		return System.currentTimeMillis() - pingTime > lastAssurance;
	}
	
	private boolean overSubstance(){
		return System.currentTimeMillis() - keepAliveTime > lastSubstance;
	}
	
	@Override
	protected void read(){
		try{
			InputStream is = socket.getInputStream();
			
			byte[] headers = new byte[4];
			int ret = is.read(headers);
			
			byte opcode = (byte) is.read();
			
			int size = Utils.bytesToInt(headers);
			if(size <= 0) return;
			byte[] payload = new byte[size];
			is.read(payload);
			
			directMessage(opcode, payload);
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void directMessage(byte opcode, byte[] payload){
		this.lastAssurance = System.currentTimeMillis();
		
		switch(opcode){
			case OPCODE_KILL:
				confirmShutdown();
				break;
			case OPCODE_KILL_RET:
				kill();
				break;
			case OPCODE_PING:
				sendPong(payload);
				break;
			case OPCODE_NONE:
				policy.receive(payload);
				setLastSubstance();
				break;
			case OPCODE_STRING_ENC:
			case OPCODE_STRING:
				policy.receive(new String(payload));
				setLastSubstance();
				break;
			case OPCODE_OBJECT:
				ByteArrayInputStream bais = new ByteArrayInputStream(payload);
				try{
					ObjectInputStream ois = new ObjectInputStream(bais);
					policy.receive(ois.readObject());
				}catch(IOException e){
					e.printStackTrace();
				}catch(ClassNotFoundException e){
					Log.log(Level.WARNING, "Improper object received.");
					e.printStackTrace();
				}
				setLastSubstance();
				break;
		}
		
		policy.receive(opcode, payload);
	}
	
	
	public int getSocketLifeCycleTime(){
		return socketLifeCycleTime;
	}
	
	public void setSocketLifeCycleTime(int socketLifeCycleTime){
		this.socketLifeCycleTime = socketLifeCycleTime;
	}
	
	public int getKeepAliveTime(){
		return keepAliveTime;
	}
	
	public void setKeepAliveTime(int keepAliveTime){
		this.keepAliveTime = keepAliveTime;
	}
	
	public int getPingTime(){
		return pingTime;
	}
	
	public void setPingTime(int pingTime){
		this.pingTime = pingTime;
	}
	
	private void setLastSubstance(){
		this.lastSubstance = System.currentTimeMillis();
	}
	
	/**
	 * Checks if the local socket is running. It does not ensure that the foreign socket is still
	 * connected or listening. If the foreign socket unexpectedly closes without alerting the local
	 * socket then this may return true. If the foreign socket gracefully exits, or if the connection
	 * times out via maximum used time or from not hearing a pong, then this will return false.
	 *
	 * @return True if the local socket still believes the connection is still established, false otherwise
	 */
	public boolean isRunning(){
		return running;
	}
	
	/**
	 * Called when the connection is closed, whether gracefully or unexpectedly.
	 */
	private void kill(){
		running = false;
		executor.shutdownNow();
		
	}
	
	@Override
	public void shutdown(){
		sendShutdown();
	}
}
