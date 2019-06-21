package tech.stdev.core2;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RichSocketKeepAliveTest{
	
	DefaultServer server;
	
	RichSocket socket;
	
	ScheduledExecutorService service;
	
	RichSocketDataForwardingPolicy policy = new RichSocketDataForwardingPolicy(){
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
		
		}
	};
	
	@BeforeAll
	public void setup() throws InterruptedException{
		this.service = Executors.newScheduledThreadPool(4);
		ScheduledExecutorService socketPool = Executors.newScheduledThreadPool(4);
		this.server = new DefaultServer(ServerDependencyFactory.localServerSocket(), s -> new RichSocket(s, policy, service));
		new Thread(server).start();
		Thread.sleep(20);
		this.socket = new RichSocket(ServerDependencyFactory.localSocket(), policy, socketPool);
		new Thread(socket).start();
	}
	
	@Test
	public void testPingTime(){
		
		
		try{
			Thread.sleep(3000);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		assertTrue(socket.isRunning());
		
		try{
			Thread.sleep(10000);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		
		assertFalse(socket.isRunning());
	}
	
	@AfterAll
	public void teardown(){
		socket.shutdown();
		server.shutdown();
		service.shutdownNow();
	}
}
