package server;

import java.awt.TextArea;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Iterator;

import swing.ServerGUI;

 
public class Server implements Runnable{
	
	public Clients clients;
	public LastMessage lastMessage;
	private ServerSocket s = null;
	
	private volatile boolean isConnected = false;
	private volatile boolean startQuery = false;
	private volatile boolean endQuery = false;
	
	private TextArea chat;
	
	public volatile String serverIP;
	public final static int PORT = 1234;
	
	public Server(TextArea c) throws NullPointerException{
		if(c != null)
			chat = c;
		else
			throw new NullPointerException();
	}
	
	
	
	public void setOn(){
		if(!this.isConnected){
			this.startQuery = true;
		}
	}
	
	public void setOff(){
		if(this.isConnected)
			this.endQuery = true;
		else
			this.flush("No server started");
	}
	
	public void flush(){
		this.chat.setText("");
	}
	
	public void flush(String s){
		this.chat.setText(s);
	}
	
	public void println(String s){
		this.chat.append(s + "\n");
	}
	
	public void getServerInfo() {
		serverIP = ServerGUI.serverIPField.getText();
	}
	
	public void run(){
		while(!Thread.currentThread().isInterrupted()){
			if(this.startQuery){
				this.flush();
				this.getServerInfo();
				this.startQuery = false;
				try{
					s = new ServerSocket(PORT, 0, InetAddress.getByName(serverIP));
					s.setSoTimeout(100);
					clients = new Clients();
					lastMessage = new LastMessage();
					
					this.println("Started: " + s);
					
					this.isConnected = true;
					while(!this.endQuery){
						Socket socket = null;
						try{
							socket = s.accept();
						}catch(SocketTimeoutException e){
							continue;
						}
						try{
							System.out.println("Connected: " + socket);
							this.println("Connected: " + socket);
							clients.addServer(new ServerOne(this, socket));
						}catch(IOException e){
							System.out.println("Closing: " + socket);
							this.println("Closing: " + socket);
							socket.close();
						}
					}
					this.endQuery = false;
				}catch(IOException e){
					e.printStackTrace();
				}finally{			
					if(this.isConnected){
						System.out.println("Close sockets");
						this.println("Close sockets");
					
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
					
						clients.clear();
						if(s != null)
							try {
								s.close();
								System.out.println("Closed: " + s);
								this.println("Closed: " + s);
								s = null;
							} catch (IOException e) {
								e.printStackTrace();
							}
						this.isConnected = false;
					}else{
						this.println("Address already in use");
						System.out.println("Address already in use");
					}
				}
			}
		}
	}
}