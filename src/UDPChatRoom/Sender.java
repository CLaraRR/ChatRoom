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
			System.out.println("������Ϣ��"+msg.getMessage());
			os.writeObject(msg);
			os.flush();
			os.close();
			byte[] sendBuf = byteStream.toByteArray();
			
			
			//���Ŀ�ķ���ip��ַ
			String destIP = msg.getDestIP();
			//���Ŀ�ķ��Ķ˿ں�
			int destPort=Integer.parseInt(msg.getDestPort());

			//����udp���ݰ��Է�������
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
			//������Ϣ
			socket.send(packet);
			//������Ϲر��׽��֡�
			socket.close();
		
		}
		catch(Throwable t) {
			t.printStackTrace();
		}
	}

}
