package Controller;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;





import swing.ClientGUI;
import client.Client;

public class ClientController{
	
	public volatile static boolean isConnected = false;
	public volatile static boolean connectQuery = false;
	public volatile static boolean disconnectQuery = false;
	public volatile static PipedInputStream pin;
	public volatile static PipedOutputStream pout;
	public volatile static String username;
	public volatile static String serverIP;
	
	public ClientController(){
		Thread clientThread = new Thread(new Client());
		clientThread.start();
		
	}
	
	public synchronized static void flush(){
		ClientGUI.chat.setText("");
	}
	
	public synchronized static void flush(String s){
		ClientGUI.chat.setText(s);
	}
	
	public synchronized static void println(String s){
		ClientGUI.chat.append(s + "\n");
	}
	
	public synchronized static void sendMsg(String s){	
		if(ClientController.isConnected){
			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(pout)), true);
			out.println(s);
			out.flush();
		}
	}
	public synchronized static void connect(){
		pin = new PipedInputStream();
		try {
			pout = new PipedOutputStream(pin);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void getClientInfo() {
		serverIP = ClientGUI.serverIPField.getText();
		username = ClientGUI.userNameField.getText();
	}

}
