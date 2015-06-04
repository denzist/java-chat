package Controller;

import java.io.IOException;

import Controller.ClientViewInterface;
import client.Client;

public class ClientController implements Runnable{
	
	private Client client;
	private ClientViewInterface view;
	
	public ClientController(ClientViewInterface view){
		this.view = view;
		try{
        	client = new Client();
			client.initMsgsQueue();
			Thread clientThread = new Thread(client);
			clientThread.start();
		}catch(IOException e){
			e.printStackTrace();
		}
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
	
	public void appendToChat(){
		try {
			view.appendToChat(client.readMsgQueue());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run(){
		while(!Thread.currentThread().isInterrupted()){
			if(client.isReady()){
				appendToChat();
			}
		}
	}

}
