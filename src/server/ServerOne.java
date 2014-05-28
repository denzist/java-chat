package server;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;


class ServerOne extends Thread{
	private Server server;
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	public String username;
	
	public ServerOne(Server server, Socket socket) throws IOException {
		this.server = server;
		this.socket = socket;
		this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
		this.server.lastMessage.send(out);
		start();
	}
	
	public void run(){
		try{
			socket.setSoTimeout(5000);
			username = in.readLine();
			if(server.clients.hasUser(username)){
				out.println("Chat has already user with name " + username);
				System.out.println("Socket closed: " + socket);
				server.addMsgToMsgsQueue("Socket closed: " + socket);
				socket.close();
				throw new IOException();
			}
			server.clients.addUser(username);
			server.clients.distribution("Joined:" + username +  " " + socket);
			server.addMsgToMsgsQueue("Joined:" + username);
			System.out.println("Joined:" + username);
			while(true){
				String s = in.readLine();
				if(!s.equals("")){
					server.addMsgToMsgsQueue(s);
					server.lastMessage.add(s);
					server.clients.distribution(s);
				}else{
					sendMsg("");
				}
				Thread.currentThread().sleep(100);
			}
		
		} catch (InterruptedException e) {
			e.printStackTrace();
		}catch(SocketTimeoutException e){
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(!socket.isClosed()){
				try {
					server.clients.distribution("Leave: " + username);
					server.addMsgToMsgsQueue("Leave: " + username);
					server.addMsgToMsgsQueue("Socket closed: " + socket);
					System.out.println("Socket closed: " + socket);
					socket.close();
					server.clients.remove(username);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
				
		}

	}
	
	public void sendMsg(String message){
		out.println(message);
		out.flush();
	}
	
	public Socket getSocket(){
		return socket;
	}
	
}
