package Controller;

import server.Server;


public class ServerController {
	
	private Server server;
	

	public ServerController(Server server){
		this.server = server;
	}
	
	public void sendStartQuery(){
		server.setOn();
	}
	
	public void sendEndQuery(){
		server.setOff();
	}

}
