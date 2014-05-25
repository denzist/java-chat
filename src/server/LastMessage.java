package server;


import java.io.*;
import java.util.*;

import message.Message;

class LastMessage{
	private LinkedList<String> list = new LinkedList<>();
	
	public void add(String msg){
		if(list.size() < 10){
			list.add(msg);
		}
		else{
			list.addLast(msg);
			list.removeFirst();
		}
		
	}
	
	public void send(PrintWriter out){
		for(String str : list){
			out.println(str);
		}
	}
}