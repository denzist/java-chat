package Controller;

public interface ClientViewInterface {
	void flush(String s);
	String getClientUserName();
	String getClientServerIP();
	void appendToChat(String s);
	
}
