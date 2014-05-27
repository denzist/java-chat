package client;

import java.awt.TextArea;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;

import javax.swing.JTextField;

import message.Message;
import server.Server;

 
public class Client implements Runnable{
	
	private TextArea chat;
	private JTextField userNameField;
	private JTextField serverIPField;
	
	private volatile boolean isConnected = false;
	private volatile boolean connectQuery = false;
	private volatile boolean disconnectQuery = false;
	
	private volatile PipedInputStream pin;
	private volatile PipedOutputStream pout;
	
	private volatile String username;
	private volatile String serverIP;
	private InetAddress addr;
	private Socket socket = null;
	
	private BufferedReader in;
	private PrintWriter out;
	
	public Client(TextArea c, JTextField u, JTextField s) throws NullPointerException{
		if(c != null)
			this.chat = c;
		else
			throw new NullPointerException();
		if(u != null)
			this.userNameField = u;
		else
			throw new NullPointerException();
		if(s != null)
			this.serverIPField = s;
		else
			throw new NullPointerException();
	}
	
	public void connect(){
		if(!this.isConnected)
			this.connectQuery = true;
	}
	
	public void disconnect(){
		if(this.isConnected){
			this.disconnectQuery = true;
		}else{
			this.flush("You are not connected to any server");
		}
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

	public synchronized void getClientInfo() {
		this.serverIP = serverIPField.getText();
		this.username = userNameField.getText();
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
							client.println(s);
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
		while(!Thread.currentThread().isInterrupted()){ // Пока поток клиента не прерван
			
			this.isConnected = false;
			
			
			try {
				Thread.currentThread().sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(this.connectQuery){
				this.flush();
				this.connectQuery = false;
				if(this.isConnected){
					this.println("Client connected");
					System.err.println("Client connected. This can't be.");
				}else{
					try {
						this.getClientInfo();
						
						addr = InetAddress.getByName(this.serverIP);
						socket = new Socket(addr, Server.PORT);
						
						try {
							socket.setSoTimeout(5000);
						} catch (SocketException e2) {
							e2.printStackTrace();
						}
						
						System.out.println("Socket: " + socket);
						this.println("Connected to server: "+ socket);
											
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
							this.println("Unexpected disconnection");
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
						this.println("Disconnected");
						
					} catch (UnknownHostException e1) {
						System.err.println("Unknow adress");
						this.flush("Unknow adress");
					} catch(IOException e){
						System.err.println("Can't connect to server");
						this.flush("Can't connect to server");
						
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
