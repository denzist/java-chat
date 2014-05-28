package server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Iterator;



 
public class Server implements Runnable{
	
	public Clients clients;
	public LastMessage lastMessage;
	private ServerSocket s = null;
	
	private volatile boolean isConnected = false;
	private volatile boolean startQuery = false;
	private volatile boolean endQuery = false;
	
	private volatile boolean isReadyMsgsQueue = false;
	private PrintWriter outMsgsQueue;
	private BufferedReader inMsgsQueue;
	
	public volatile String serverIP;
	public final static int PORT = 1234;
	
	public boolean setOn(){
		if(!this.isConnected){
			this.startQuery = true;
			return true;
		}
		return false;
	}
	
	public boolean isReady(){
		return isReadyMsgsQueue;
	}
	
	public boolean setOff(){
		if(this.isConnected){
			this.endQuery = true;
			return true;
		}
		return false;
	}
	
	public void setServerInfo(String serverIP) {
		this.serverIP = serverIP; 
	}
	
	public void initMsgsQueue() throws IOException{
		PipedInputStream pinMsgs = new PipedInputStream();
		PipedOutputStream poutMsgs = new PipedOutputStream(pinMsgs);
		this.outMsgsQueue = new PrintWriter(new BufferedWriter(new OutputStreamWriter(poutMsgs)), true);
		this.inMsgsQueue = new BufferedReader(new InputStreamReader(pinMsgs));
		this.isReadyMsgsQueue = true;
	}
	
	public synchronized void addMsgToMsgsQueue(String msg){
		this.outMsgsQueue.println(msg);
		this.outMsgsQueue.flush();
	}
	
	public String readMsgQueue() throws IOException{
		String s = this.inMsgsQueue.readLine();
		return s;
	}

	
	public void run(){
		while(!Thread.currentThread().isInterrupted()){
			if(this.startQuery){
				this.startQuery = false;
				try{
					s = new ServerSocket(PORT, 0, InetAddress.getByName(serverIP));
					s.setSoTimeout(100);
					clients = new Clients();
					lastMessage = new LastMessage();
					
					this.addMsgToMsgsQueue("Started: " + s);
					
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
							this.addMsgToMsgsQueue("Connected: " + socket);
							clients.addServer(new ServerOne(this, socket));
						}catch(IOException e){
							System.out.println("Closing: " + socket);
							this.addMsgToMsgsQueue("Closing: " + socket);
							socket.close();
						}
					}
					this.endQuery = false;
				}catch(IOException e){
					e.printStackTrace();
				}finally{			
					if(this.isConnected){
						System.out.println("Close sockets");
						this.addMsgToMsgsQueue("Close sockets");
					
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
						System.out.println("asds");
						clients.clear();
						
						if(s != null)
							try {
								s.close();
								System.out.println("Closed: " + s);
								this.addMsgToMsgsQueue("Closed: " + s);
								s = null;
							} catch (IOException e) {
								e.printStackTrace();
							}
						this.isConnected = false;
					}else{
						if(isReadyMsgsQueue){
							this.addMsgToMsgsQueue("Address already in use");
							System.out.println("Address already in use");
						}else{
							System.err.println("Error in initMsgsQueue");
						}
					}
				}
			}
		}
	}
}