package swing;


import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

import java.awt.TextArea;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;

import Controller.ClientController;
import Controller.ClientViewInterface;


public class ClientGUI implements ClientViewInterface{
	
	private ClientController clientController;

	private JFrame frame;
	private JTextField messageField;
	private JTextField userNameField;
	private JTextField serverIPField;
	private TextArea chat;
	private JButton btnConnect;
	private JButton btnDisconnect;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientGUI window = new ClientGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	public void appendToChat(String msg){
		chat.append(msg + "\n");
	}
	
	public ClientGUI(){
		init();
	}
	
	@Override
	public void flush(String s){
		chat.setText(s);
	}

	@Override
	public String getClientUserName(){
		return userNameField.getText();
	}
	
	@Override
	public String getClientServerIP(){
		return serverIPField.getText();
	}
	
	private void init() {
		frame = new JFrame();
		frame.setResizable(false);
		frame.getContentPane().setLayout(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(490, 468));
		
		messageField = new JTextField();
		messageField.setBounds(21, 347, 417, 47);
		frame.getContentPane().add(messageField);
		messageField.setColumns(10);
		messageEvent enter = new messageEvent();
		messageField.addKeyListener(enter);
		
		JButton btnSend = new JButton("Send");
		btnSend.setBounds(349, 405, 89, 23);
		btnSend.setActionCommand("Send action");
		frame.getContentPane().add(btnSend);
		SendEvent send = new SendEvent();
		btnSend.addActionListener(send);
		
		userNameField = new JTextField("user");
		userNameField.setBounds(21, 39, 147, 20);
		frame.getContentPane().add(userNameField);
		userNameField.setColumns(10);
		
		JLabel lblUserName = new JLabel("User name");
		lblUserName.setBounds(21, 14, 147, 14);
		frame.getContentPane().add(lblUserName);
		
		serverIPField = new JTextField("localhost");
		serverIPField.setBounds(254, 39, 147, 20);
		frame.getContentPane().add(serverIPField);
		serverIPField.setColumns(10);
		
		JLabel lblServerIp = new JLabel("Server IP");
		lblServerIp.setBounds(253, 14, 89, 14);
		frame.getContentPane().add(lblServerIp);
		
		JLabel lblChat = new JLabel("Chat");
		lblChat.setBounds(21, 70, 110, 29);
		frame.getContentPane().add(lblChat);
		
		chat = new TextArea();
		chat.setBounds(21, 105, 417, 212);
		frame.getContentPane().add(chat);
		
		btnConnect = new JButton("Connect");
		btnConnect.setBounds(78, 70, 89, 23);
		frame.getContentPane().add(btnConnect);
		btnConnect.setActionCommand("Connect action");
		ConnectEvent connect = new ConnectEvent();
		btnConnect.addActionListener(connect);
		
		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.setBounds(235, 70, 107, 23);
		frame.getContentPane().add(btnDisconnect);
		btnDisconnect.setActionCommand("Disconnect action");
		DisconnectEvent disconnect = new DisconnectEvent();
		btnDisconnect.addActionListener(disconnect);
		
		frame.pack();
        frame.setVisible(true);   
        
        clientController = new ClientController(this);
				
		Thread clientControlThread = new Thread(clientController);
		clientControlThread.start();
	}
	
	public class messageEvent implements KeyListener {
        
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER){
				clientController.sendMsg(messageField.getText());
				messageField.setText("");
			}
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}
    }
	
	public class SendEvent implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand() == "Send action"){
				clientController.sendMsg(messageField.getText());
				messageField.setText("");
			}
		}
	}

	public class DisconnectEvent implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand() == "Disconnect action")
				clientController.sendDisconnectQuery();
		}
		
	}
	
	public class ConnectEvent implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand() == "Connect action")
				clientController.sendConnectQuery();
		}
		
	}
}