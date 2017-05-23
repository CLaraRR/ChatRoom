package TCPMultiChatRoom;


import java.net.Socket;

public class User {

	private String UserName="";
	private Socket socket;
	
	public String getUserName() {
		return UserName;
	}
	public void setUserName(String userName) {
		UserName = userName;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	} 
	
	
}
