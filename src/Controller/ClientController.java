package Controller;

import client.Client;

public class ClientController{
	
	private Client client;
	
	public ClientController(Client client){
		this.client = client;
	}
	
	public void sendConnectQuery(){
		client.connect();
	}
	
	public void sendDisconnectQuery(){
		client.disconnect();
	}
	
	public void sendMsg(String s){
		client.sendMsg(s);
	}

}
