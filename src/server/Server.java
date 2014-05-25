package server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Iterator;

import Controller.ServerController;
 
public class Server implements Runnable{
	
	public static Clients clients;
	public static LastMessage lastMessage;
	private ServerSocket s = null;
	
	
	public void run(){
		while(!Thread.currentThread().isInterrupted()){
			if(ServerController.startQuery){
				ServerController.flush();
				ServerController.getServerInfo();
				ServerController.startQuery = false;
				try{
					s = new ServerSocket(ServerController.PORT, 0, InetAddress.getByName(ServerController.serverIP));
					s.setSoTimeout(5);
					clients = new Clients();
					lastMessage = new LastMessage();
					
					//System.out.println("Started: " + s);
					ServerController.println("Started: " + s);
					
					ServerController.isConnected = true;
					while(!ServerController.endQuery){
						Socket socket = null;
						try{
							socket = s.accept();
						}catch(SocketTimeoutException e){
							continue;
						}
						try{
							//System.out.println("Connected: " + socket);
							ServerController.println("Connected: " + socket);
							clients.addUser(new ServerOne(socket));
						}catch(IOException e){
							System.out.println("Closing: " + socket);
							ServerController.println("Closing: " + socket);
							socket.close();
						}
					}
					ServerController.endQuery = false;
				}catch(IOException e){
					e.printStackTrace();
				}finally{			
					if(ServerController.isConnected){
						System.out.println("Close sockets");
						ServerController.println("Close sockets");
					
						Iterator<ServerOne> iterClients = clients.servers.iterator();					
						while(iterClients.hasNext()){
						
							ServerOne serverOne = iterClients.next();
							serverOne.interrupt();
							try {
							serverOne.join();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					
						clients.servers.clear();
						if(s != null)
							try {
								s.close();
								System.out.println("Closed: " + s);
								ServerController.println("Closed: " + s);
								s = null;
							} catch (IOException e) {
								e.printStackTrace();
							}
						ServerController.isConnected = false;
					}else{
						ServerController.println("Address already in use");
						System.out.println("Address already in use");
					}
				}
			}
		}
	}
}