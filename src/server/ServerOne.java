package server;

import message.Message;
import message.Ping;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

import Controller.ServerController;

class ServerOne extends Thread{
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	public String username;
	
	public ServerOne(Socket s) throws IOException {
		socket = s;
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
		Server.lastMessage.send(out);
		start();
	}
	
	public void run(){
		try{
			socket.setSoTimeout(1000);
			username = in.readLine();
			Server.clients.distribution("Joined:" + username);
			ServerController.println("Joined:" + username);
			System.out.println("Joined:" + username);
			while(!Thread.currentThread().isInterrupted()){
				String s = in.readLine();
				if(!s.equals("")){
					ServerController.println(s);
					Server.lastMessage.add(s);
					Server.clients.distribution(s);
				}else{
					out.println("");
					out.flush();
				}
			}
		}catch(SocketTimeoutException e){
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(!socket.isClosed()){
				try {
					Server.clients.distribution("Leave: " + username);
					ServerController.println("Leave: " + username);
					System.out.println("Socket closed: " + socket);
					ServerController.println("Socket closed: " + socket);
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
				
		}
	}
	
	public void sendMsg(String message){
		out.println(message);
	}
	
	public Socket getSocket(){
		return socket;
	}
	
}
