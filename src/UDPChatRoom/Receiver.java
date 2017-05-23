package UDPChatRoom;

import java.net.*;
import java.io.*;
 
public class Receiver extends Thread {

	private Message msg;
	private DatagramPacket packet;
	private DatagramSocket socket;
	private byte[] recvBuf = new byte[500];
	private ChatClient chatclient;

	public Receiver(ChatClient chatclient) {
		this.chatclient=chatclient;
		try{
			passiveUDP(chatclient.localport);
			//创建udp包以接受数据。
			packet = new DatagramPacket(recvBuf,recvBuf.length);
			
		}
		catch(Exception e){
			
		}
	}
 
	private void passiveUDP(int port) {
		// TODO Auto-generated method stub
		//创建接受方的udp端口以接收数据
		try {
			socket = new DatagramSocket(port);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("passiveUDP error!");
		} 
		
	}

	public void run() {
		try {
			while(true){
				//接受数据
				socket.receive(packet);
 
				ByteArrayInputStream byteStream = new ByteArrayInputStream(recvBuf);
				ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
				Object o = is.readObject();
				msg = (Message)o;
				is.close();
 
				String str=chatclient.chatArea.getText();
				chatclient.chatArea.setText(str+msg.getMessage());
				System.out.println("收到消息："+msg.getMessage());
			}
		}
		catch(Throwable t) {
			t.printStackTrace();
		}
	}
}
