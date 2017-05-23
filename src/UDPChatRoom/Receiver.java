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
			//����udp���Խ������ݡ�
			packet = new DatagramPacket(recvBuf,recvBuf.length);
			
		}
		catch(Exception e){
			
		}
	}
 
	private void passiveUDP(int port) {
		// TODO Auto-generated method stub
		//�������ܷ���udp�˿��Խ�������
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
				//��������
				socket.receive(packet);
 
				ByteArrayInputStream byteStream = new ByteArrayInputStream(recvBuf);
				ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
				Object o = is.readObject();
				msg = (Message)o;
				is.close();
 
				String str=chatclient.chatArea.getText();
				chatclient.chatArea.setText(str+msg.getMessage());
				System.out.println("�յ���Ϣ��"+msg.getMessage());
			}
		}
		catch(Throwable t) {
			t.printStackTrace();
		}
	}
}
