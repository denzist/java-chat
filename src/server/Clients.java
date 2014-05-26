package server;

import java.util.*;


class Clients{
	
	public ArrayList<ServerOne> servers = new ArrayList<ServerOne>();
	public ArrayList<String> usernames = new ArrayList<String>();
	
	public synchronized void addServer(ServerOne server){
		servers.add(server);		
	}
	
	public synchronized void addUser(String username){
		usernames.add(username);
	}
	
	public synchronized void clear(){
		servers.clear();
		usernames.clear();
	}
	
	public synchronized boolean hasUser(String username){
		return usernames.contains(username);
	}
	
	public void distribution(String msg){
		Iterator<ServerOne> iter = servers.iterator();
		while(iter.hasNext()){
			ServerOne elem = iter.next();
			elem.sendMsg(msg);

		}
	}
}
