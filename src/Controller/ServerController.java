package Controller;

import java.io.IOException;

import server.Server;

public class ServerController implements Runnable {
	
	private Server server;
	private ServerViewInterface view;
	

	public ServerController(ServerViewInterface view){
		this.view = view;
		try{
			server = new Server();
			server.initMsgsQueue();
			Thread serverThread = new Thread(server);
			serverThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendStartQuery(){
		if(server.setOn()){
			view.flush("");
			server.setServerInfo(view.getServerInfo());
		}
	}
	
	public void appendToChat(){
		try {
			view.appendToChat(server.readMsgQueue());
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
	
	public void sendEndQuery(){
		server.setOff();
	}
	

	@Override
	public void run(){
		while(!Thread.currentThread().isInterrupted()){
			if(server.isReady()){
				appendToChat();
			}
		}
	}

}
