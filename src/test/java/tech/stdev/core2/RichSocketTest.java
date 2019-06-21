package tech.stdev.core2;

import org.junit.jupiter.api.*;

import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RichSocketTest{
	
	DefaultServer server;
	
	RichSocket socket;
	
	ScheduledExecutorService service;
	
	RichSocketDataForwardingPolicy serverPolicy = new RichSocketDataForwardingPolicy(){
		@Override
		public void receive(String string){
			RichSocketTest.this.gotString = string;
		}
		
		@Override
		public void receive(Object deserialized){
			RichSocketTest.this.gotSerializeable = (Serializable) deserialized;
		}
		
		@Override
		public void receive(byte[] payload){
			RichSocketTest.this.gotBytes = payload;
		}
		
		@Override
		public void receive(byte opcode, byte[] payload){
			switch(opcode){
				case RichSocket.OPCODE_PING:
					RichSocketTest.this.gotPinged = true;
					break;
				case RichSocket.OPCODE_KILL:
					RichSocketTest.this.gotShutdown = true;
			}
		}
	};
	
	RichSocketDataForwardingPolicy socketPolicy = new RichSocketDataForwardingPolicy(){
		@Override
		public void receive(String string){
		
		}
		
		@Override
		public void receive(Object deserialized){
		
		}
		
		@Override
		public void receive(byte[] payload){
		
		}
		
		@Override
		public void receive(byte opcode, byte[] payload){
			switch(opcode){
				case RichSocket.OPCODE_PONG:
					RichSocketTest.this.didPong = true;
					break;
				case RichSocket.OPCODE_KILL_RET:
					RichSocketTest.this.confirmedShutdown = true;
			}
		}
	};
	
	@BeforeAll
	public void setup() throws InterruptedException{
		this.service = Executors.newScheduledThreadPool(4);
		ScheduledExecutorService socketPool = Executors.newScheduledThreadPool(4);
		this.server = new DefaultServer(ServerDependencyFactory.localServerSocket(), s -> new RichSocket(s, serverPolicy, service));
		new Thread(server).start();
		Thread.sleep(20);
		this.socket = new RichSocket(ServerDependencyFactory.localSocket(), socketPolicy, socketPool);
		new Thread(socket).start();
		sleep(20);
		
		
		socket.send((this.sendBytes = TestUtils.randomBytes(1000)));
		sleep(5);
		socket.send((this.sendString = "Hello World!"));
		sleep(5);
		socket.send((this.sendObject = new TestSerializer()));
		sleep(5);
		socket.sendPing();
		sleep(50);
		socket.sendShutdown();
		sleep(500);
	}
	
	private byte[] sendBytes;
	private byte[] gotBytes;
	
	@Test
	void send(){
		assertArrayEquals(sendBytes, gotBytes);
	}
	
	private String sendString;
	private String gotString;
	
	@Test
	void send1(){
		assertEquals(gotString, this.sendString);
	}
	
	private Serializable sendObject;
	private Serializable gotSerializeable;
	
	@Test
	void send2(){
		assertEquals(gotSerializeable, sendObject);
	}
	
	private boolean gotPinged = false;
	
	@Test
	void sendPing(){
		assertTrue(gotPinged);
	}
	
	private boolean didPong = false;
	
	@Test
	void sendPong(){
		assertTrue(didPong);
	}
	
	private boolean gotShutdown = false;
	
	@Test
	void sendShutdown(){
		assertTrue(gotShutdown);
	}
	
	@Test
	void sendingAfterShutdown(){
		assertFalse(socket.send("Should Fail"));
	}
	
	private boolean confirmedShutdown;
	
	@Test
	void confirmShutdown(){
		assertTrue(confirmedShutdown);
	}
	
	@AfterAll
	public void shutdown() throws InterruptedException{
		socket.shutdown();
		service.shutdownNow();
		server.shutdown();
	}
}