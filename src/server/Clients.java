package server;

import java.util.*;

import message.Message;

class Clients{
	
	public ArrayList<ServerOne> servers = new ArrayList<>();
	
	public void addUser(ServerOne server){
		servers.add(server);
		
	}
	
	public void distribution(String msg){
		Iterator<ServerOne> iter = servers.iterator();
		while(iter.hasNext()){
			ServerOne elem = iter.next();
			elem.sendMsg(msg);

		}
	}
}
