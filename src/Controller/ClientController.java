package Controller;

import swing.ClientGUI;
import client.Client;

public class ClientController implements Runnable{
	
	private Client client;
	private ClientGUI view;
	
	public ClientController(Client client, ClientGUI view){
		this.client = client;
		this.view = view;
	}
	
	public void sendConnectQuery(){
		if(client.connect()){
			view.flush("");
			client.setClientInfo(view.getClientUserName(), view.getClientServerIP());
		}
	}
	
	public void sendDisconnectQuery(){
		if(!client.disconnect()){
			view.flush("You are not connected to server");
		}
	}
	
	public void sendMsg(String s){
		client.sendMsg(s);
	}
	
	@Override
	public void run(){
		while(!Thread.currentThread().isInterrupted()){
			if(client.isReady()){
				view.showMsg();
			}
		}
		
	}

}
