package client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;

import message.Message;
import server.Server;

 
public class Client implements Runnable{
	

	private volatile boolean isConnected = false;
	private volatile boolean connectQuery = false;
	private volatile boolean disconnectQuery = false;
	
	private volatile boolean isReadyMsgsQueue = false;
	private volatile PipedInputStream pin;
	private volatile PipedOutputStream pout;
	
	private PrintWriter outMsgsQueue;
	private BufferedReader inMsgsQueue;
	
	private volatile String username;
	private volatile String serverIP;
	private InetAddress addr;
	private Socket socket = null;
	
	private BufferedReader in;
	private PrintWriter out;
	
	public boolean isConnected(){
		return isConnected;
	}
	
	public boolean connect(){
		if(!this.isConnected){
			this.connectQuery = true;
			return true;
		}
		return false;
	}
	
	public boolean disconnect(){
		if(this.isConnected){
			this.disconnectQuery = true;
			return true;
		}
		return false;
	}
	
	public void setClientInfo(String u, String ip){
		this.username = u;
		this.serverIP = ip;
	}
	
	public boolean isReady(){
		return isReadyMsgsQueue;
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

	
	public void sendMsg(String s){	
		if(this.isConnected){
			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(pout)), true);
			out.println(s);
			out.flush();
		}
	}
	
	public void initPinPout(){
		this.pin = new PipedInputStream();
		try {
			this.pout = new PipedOutputStream(pin);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public class ReadMessage implements Runnable{
		private String s = null;
		private Client client = null;
		
		public ReadMessage(Client c){
			this.client = c;
		}
		
		@Override
		public void run(){
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				while(!Thread.currentThread().isInterrupted()){
					s = in.readLine();
					if(!s.equals("")){
							client.addMsgToMsgsQueue(s);
					}
				}
				System.out.println("ReadMsg: interrupted");
			}catch(SocketTimeoutException e){
				System.out.println("ReadMsg: Cought socket exeption");
			} catch (IOException e) {
				System.out.println("ReadMsg: Cought  IO exeption");
			}catch(NullPointerException e){
				System.out.println("ReadMsg: Cought  nullpointer exeption");
			}finally{
				this.client.isConnected = false;
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public class WriteMessage implements Runnable{
		private Client client = null;
		
		public WriteMessage(Client c){
			this.client = c;
		}
		
		@Override
		public void run(){
			try {
				out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
				out.println(this.client.username);
				this.client.initPinPout();
				BufferedReader in = new BufferedReader(new InputStreamReader(this.client.pin));
				
				while(!Thread.currentThread().isInterrupted()){
					if(in.ready()){
						String s = in.readLine();
						Message msg = new Message(this.client.username, s, new Date());
						out.println(msg.toString());
						out.flush();
					}else{
						out.println("");
						out.flush();
					}
					Thread.currentThread().sleep(100);
				}
			} catch (InterruptedException e) {
				System.out.println("WriteMsg: interrupted");
			} catch (IOException e) {
				System.out.println("WriteMsg: Cought exeption");
			}finally{
				this.client.isConnected = false;
				out.close();
			}
		}
	}
	
	
	@SuppressWarnings("finally")
	@Override
	public void run(){
		System.out.println("client thread started");
		while(!Thread.currentThread().isInterrupted()){
			this.isConnected = false;
			try {
				Thread.currentThread().sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(this.connectQuery){
				this.connectQuery = false;
				if(this.isConnected){
					this.addMsgToMsgsQueue("Client connected");
					System.err.println("Client connected. This can't be.");
				}else{
					try {
					
						addr = InetAddress.getByName(this.serverIP);
						socket = new Socket(addr, Server.PORT);
						
						try {
							socket.setSoTimeout(5000);
						} catch (SocketException e2) {
							e2.printStackTrace();
						}
						
						System.out.println("Socket: " + socket);
						this.addMsgToMsgsQueue("Connected to server: "+ socket);
											
						this.isConnected = true;
						
						Thread readThread = new Thread(new ReadMessage(this)); 
						readThread.start();
						
						Thread writeThread = new Thread(new WriteMessage(this));
						writeThread.start();
						
						while(!this.disconnectQuery && this.isConnected){
							Thread.currentThread().sleep(100);
						}
						
						if(!this.isConnected){
							System.out.println("Unexpected disconnection");
							this.addMsgToMsgsQueue("Unexpected disconnection");
						}
						if(this.disconnectQuery){
							this.disconnectQuery = false;
							this.isConnected = false;
							socket.close();
							System.out.println("Close socket:" + socket);
						}
												
						readThread.interrupt();
						readThread.join();
						
						writeThread.interrupt();
						writeThread.join();
						
						System.out.println("Disconnected");
						this.addMsgToMsgsQueue("Disconnected");
						
					} catch (UnknownHostException e1) {
						System.err.println("Unknow adress");
						this.addMsgToMsgsQueue("Unknow adress");
					} catch(IOException e){
						System.err.println("Can't connect to server");
						this.addMsgToMsgsQueue("Can't connect to server");
						
					}finally{
						if(socket != null)
							if(!socket.isClosed())
								try{
									System.out.println("Close socket:" + socket);
									socket.close();
									socket = null;
								} catch (IOException e) {
									e.printStackTrace();
								}
						continue;
					}
				}
			}
		}
		System.out.println("client thread ended");
	}
 
	
}
