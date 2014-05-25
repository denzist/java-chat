package Controller;

import swing.ClientGUI;
import swing.ServerGUI;
import server.Server;


public class ServerController {
	
	public volatile static boolean isConnected;
	public volatile static boolean startQuery = false;
	public volatile static boolean endQuery = false;
	public volatile static String serverIP;
	public final static int PORT = 1234;

	public ServerController(){
		Thread serverThread = new Thread(new Server());
		serverThread.start();
	}
	
	public synchronized static void flush(){
		ServerGUI.chat.setText("");
	}
	
	public synchronized static void flush(String s){
		ServerGUI.chat.setText(s);
	}
	
	public synchronized static void println(String s){
		ServerGUI.chat.append(s + "\n");
	}
	
	public static void getServerInfo() {
		serverIP = ServerGUI.serverIPField.getText();
	}

}
