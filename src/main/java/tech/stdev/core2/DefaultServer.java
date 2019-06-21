package tech.stdev.core2;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class DefaultServer extends AbstractServer{
	
	//Java recommended, not a great heuristic but meh it can be overriden
	private static int DEFAULT_THREADS = Runtime.getRuntime().availableProcessors() * 2;
	
	public DefaultServer(ServerSocket serverSocket, SocketFactory factory){
		super(serverSocket);
		this.factory = factory;
		this.executor = Executors.newFixedThreadPool(DEFAULT_THREADS);
	}
	
	private final ExecutorService executor;
	
	private final SocketFactory factory;
	
	@Override
	protected void onNewSocket(Socket socket){
		executor.submit(() -> factory.create(socket).run());
	}
}
