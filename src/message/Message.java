package message;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
 
public class Message implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String username;
	private String message;
	private Date date;
	
	public Message(String username, String message, Date date){
		this.message = message;
		this.username = username;
		this.date = date;
	}
	
	public String toString(){
		String s = "";
		if(date != null){
			DateFormat df = new SimpleDateFormat("hh:mm");
			s = " (" + df.format(date) + ") :";
		}
		return username + s + message;
	}
	
	public String getName(){
		return username;
	}
	
	public String getMessage(){
		return message;
	}
	
	public Date gerDate(){
		return date;
	}
	
}