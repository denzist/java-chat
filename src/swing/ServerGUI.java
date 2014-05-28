package swing;


import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

import java.awt.TextArea;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.JLabel;

import server.Server;
import Controller.ServerController;



public class ServerGUI {
	
	
	private ServerController serverController;
	private Server server;
	public JFrame frame;
	private static JTextField serverIPField;
	private static TextArea chat;
	public JButton btnConnect;
	public JButton btnDisconnect;

	public String getServerInfo(){
		return serverIPField.getText();
	}
	
	public void flush(String s){
		chat.setText(s);
	}
	
	public void showMsg(){
		try {
			chat.append(server.readMsgQueue() + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerGUI window = new ServerGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}
	
	public ServerGUI(){
		init();
	}
	
	private void init() {
		frame = new JFrame();
		frame.setResizable(false);
		frame.getContentPane().setLayout(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(490, 468));
		
		serverIPField = new JTextField("localhost");
		serverIPField.setBounds(44, 39, 218, 20);
		frame.getContentPane().add(serverIPField);
		serverIPField.setColumns(10);
		
		JLabel lblServerIp = new JLabel("Server IP");
		lblServerIp.setBounds(44, 11, 89, 14);
		frame.getContentPane().add(lblServerIp);
		
		chat = new TextArea();
		chat.setBounds(21, 105, 435, 311);
		frame.getContentPane().add(chat);
		
		btnConnect = new JButton("Start server");
		btnConnect.setBounds(54, 70, 147, 23);
		frame.getContentPane().add(btnConnect);
		btnConnect.setActionCommand("Start action");
		StartEvent start = new StartEvent();
		btnConnect.addActionListener(start);
		
		btnDisconnect = new JButton("Close server");
		btnDisconnect.setBounds(287, 70, 107, 23);
		frame.getContentPane().add(btnDisconnect);
		btnDisconnect.setActionCommand("Close server action");
		DisconnectEvent disconnect = new DisconnectEvent();
		btnDisconnect.addActionListener(disconnect);
		
		frame.pack();
        frame.setVisible(true);   
        try{
			server = new Server();
			server.initMsgsQueue();
			serverController = new ServerController(server, this);
			
			Thread serverThread = new Thread(server);
			serverThread.start();
			
			Thread serverControlThread = new Thread(serverController);
			serverControlThread.start();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class DisconnectEvent implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand() == "Close server action"){	
				serverController.sendEndQuery();
			}
			
		}
		
	}
	
	public class StartEvent implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand() == "Start action"){
				serverController.sendStartQuery();
			}
		}
	}

}