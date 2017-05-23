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
	
	//�����Ϣ����
	public String getMessage() {
		return msg;
	}
	
	//��÷��ͷ���ip
	public String getIP() {
		return ip;
	}
	
	//��ý��ܷ���ip
	public String getDestIP() {
		return destip;
	}
	
	//��ý��շ��Ķ˿ں�
	public String getDestPort(){
		return destport;
	}
	
}
