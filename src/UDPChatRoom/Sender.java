package UDPChatRoom;

import java.net.*;
import java.io.*;
 
public class Sender extends Thread {
 
	private Message msg;
	private DatagramSocket socket;
	private DatagramPacket packet;
 
	public Sender(Message msg) {
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(500);
			ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
			System.out.println("发送消息："+msg.getMessage());
			os.writeObject(msg);
			os.flush();
			os.close();
			byte[] sendBuf = byteStream.toByteArray();
			
			
			//获得目的方的ip地址
			String destIP = msg.getDestIP();
			//获得目的方的端口号
			int destPort=Integer.parseInt(msg.getDestPort());

			//创建udp数据包以发送数据
			packet = new DatagramPacket(sendBuf,sendBuf.length,
							InetAddress.getByName(destIP),destPort);
			socket = new DatagramSocket();
		}
		catch(Throwable t) {
			t.printStackTrace();
			System.out.println("Sender init error!");
		}
	}
 

	public void run() {
		try {
			//发送消息
			socket.send(packet);
			//发送完毕关闭套接字。
			socket.close();
		
		}
		catch(Throwable t) {
			t.printStackTrace();
		}
	}

}
