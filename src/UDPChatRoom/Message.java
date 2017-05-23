package UDPChatRoom;

import java.io.Serializable;

public class Message implements Serializable{
	 
	private String msg;
	private String ip;
	private String destip;
	private String destport;
 
	public Message(String msg, String ip, String destip,String destport) {
		this.msg = msg;
		this.ip = ip;
		this.destip = destip;
		this.destport=destport;
	}
	
	//获得消息内容
	public String getMessage() {
		return msg;
	}
	
	//获得发送方的ip
	public String getIP() {
		return ip;
	}
	
	//获得接受方的ip
	public String getDestIP() {
		return destip;
	}
	
	//获得接收方的端口号
	public String getDestPort(){
		return destport;
	}
	
}
