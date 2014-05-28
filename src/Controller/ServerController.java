package Controller;

import server.Server;
import swing.ServerGUI;


public class ServerController implements Runnable {
	
	private Server server;
	private ServerGUI view;
	

	public ServerController(Server server, ServerGUI view){
		this.server = server;
		this.view = view;
	}
	
	public void sendStartQuery(){
		if(server.setOn()){
			view.flush("");
			server.setServerInfo(view.getServerInfo());
		}
	}
	
	public void sendEndQuery(){
		server.setOff();
	}
	

	@Override
	public void run(){
		while(!Thread.currentThread().isInterrupted()){
			if(server.isReady()){
				view.showMsg();
			}
		}
		
	}

}
