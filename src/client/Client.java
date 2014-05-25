package client;

import Controller.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;

import message.Message;
import message.Ping;
import server.Server;

 
public class Client implements Runnable{
	
	private InetAddress addr;
	private Socket socket = null;
	private BufferedReader in;
	private PrintWriter out;
	
	public class ReadMessage implements Runnable{
		private String s = null;
		
		@Override
		public void run(){
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				while(!Thread.currentThread().isInterrupted()){
					s = in.readLine();
					if(!s.equals("")){
							ClientController.println(s);
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
				ClientController.isConnected = false;
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
	}

	public class WriteMessage implements Runnable{
		 
		
		@Override
		public void run(){
			try {
				out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
				out.println(ClientController.username);
				ClientController.connect();
				BufferedReader in = new BufferedReader(new InputStreamReader(ClientController.pin));
				
				while(!Thread.currentThread().isInterrupted()){
					if(in.ready()){
						String s = in.readLine();
						System.out.println("//"+s);
						Message msg = new Message(ClientController.username, s, new Date());
						out.println(msg.toString());
						out.flush();
					}else{
						out.println("");
						out.flush();
					}
				}
				System.out.println("WriteMsg: interrupted");
			} catch (IOException e) {
				System.out.println("WriteMsg: Cought exeption");
			}finally{
				ClientController.isConnected = false;
				out.close();
			}
		}
	}
	
	
	@SuppressWarnings("finally")
	@Override
	public void run(){
		System.out.println("client thread started");
		while(!Thread.currentThread().isInterrupted()){ // Пока поток клиента не прерван
			
			ClientController.isConnected = false;
			
			
			try {
				Thread.currentThread().sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(ClientController.connectQuery){
				ClientController.flush();
				ClientController.connectQuery = false;
				if(ClientController.isConnected){
					ClientController.println("Client connected");
					System.err.println("Client connected. This can't be.");
				}else{
					try {
						ClientController.getClientInfo();
						
						addr = InetAddress.getByName(ClientController.serverIP);
						socket = new Socket(addr, ServerController.PORT);
						
						try {
							socket.setSoTimeout(1000);
						} catch (SocketException e2) {
							e2.printStackTrace();
						}
						
						System.out.println("Socket: " + socket);
						ClientController.println("Connected to server: "+ socket);
											
						ClientController.isConnected = true;
						
						Thread readThread = new Thread(new ReadMessage()); 
						readThread.start();
						
						Thread writeThread = new Thread(new WriteMessage());
						writeThread.start();
						
						while(!ClientController.disconnectQuery && ClientController.isConnected){
							Thread.currentThread().sleep(100);
						}
						
						if(!ClientController.isConnected){
							System.out.println("Unexpected disconnection");
							ClientController.println("Unexpected disconnection");
						}
						if(ClientController.disconnectQuery){
							ClientController.disconnectQuery = false;
							ClientController.isConnected = false;
							socket.close();
							System.out.println("Close socket:" + socket);
						}
												
						readThread.interrupt();
						readThread.join();
						
						writeThread.interrupt();
						writeThread.join();
						
						System.out.println("Disconnected");
						ClientController.println("Disconnected");
						
					} catch (UnknownHostException e1) {
						System.err.println("Unknow adress");
						ClientController.flush("Unknow adress");
					} catch(IOException e){
						System.err.println("Can't connect to server");
						ClientController.flush("Can't connect to server");
						
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
