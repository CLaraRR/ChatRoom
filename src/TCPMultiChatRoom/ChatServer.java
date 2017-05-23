package TCPMultiChatRoom;


import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

import net.sf.json.JSONObject;

public class ChatServer extends JFrame {
 
  private static final long serialVersionUID = 1L;
  // ������������ʱ���½�һ��������
  //����һ���û���Ϣ�б�
  ArrayList<User> clientList = new ArrayList<User>();
  //���������û����б�
  ArrayList<String> usernamelist = new ArrayList<String>();
  //����һ����Ϣ��ʾ��
  private JTextArea jta = new JTextArea();
  //����һ���û����󣬸����������������� socket��username��
  private User user = null;
  //����һ�������
  DataOutputStream output = null;
  //����һ��������
  DataInputStream input = null;
  
  public static void main(String[] args) {
    new ChatServer();
  }

  public ChatServer() {
    // ������Ϣ��ʾ�����
    setLayout(new BorderLayout());
    add(new JScrollPane(jta), BorderLayout.CENTER);
    jta.setEditable(false);
    setTitle("TCP������������� ");
    setSize(500,300);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true); // 

    try {
      // ����һ��������socket���󶨶˿�8000
      ServerSocket serverSocket = new ServerSocket(8888);
      //��ӡ����ʱ��
      jta.append("����������ʱ�� " + new Date() + '\n');
     //����ѭ�������Ƿ����µĿͻ�������
      while (true) {
        // ����һ���µ�����
        Socket socket = serverSocket.accept();
        if(socket!=null){
        	//��ȡ�����û�����Ϣ
            input = new DataInputStream(socket.getInputStream());
            String json = input.readUTF();
            JSONObject data = JSONObject.fromObject(json.toString());
            System.out.println("###"+data.getString("username"));
            jta.append("�û�:" + data.getString("username") +
              "��" + new Date()+"��½ϵͳ" + '\n');
           //��ʾ�û���¼ip��ַ
            InetAddress inetAddress = socket.getInetAddress();
            jta.append("�û�" + data.getString("username") + "��IP��ַ�ǣ�"
              + inetAddress.getHostAddress() + "\n");
           //�½�һ���û�����
            user = new User();
            //���ø��û������socket
        	user.setSocket(socket);
        	//�����û���
        	user.setUserName(data.getString("username"));
        	//���������û����б�
        	clientList.add(user);
        	//�����û����б��û���ʾ�ڿͻ��˵��û��б�
        	usernamelist.add(data.getString("username"));
        }
        //�û�������ʾ�������json��ʽ����
        JSONObject online = new JSONObject();
        online.put("userlist", usernamelist);
        online.put("msg", user.getUserName()+"������");
        
        //��ʾ�����û����µ��û�����
        for (int i = 0; i < clientList.size(); i++) {
        	try{
            User otheruser = clientList.get(i);
            //��ȡÿһ���û���socket���õ��������
                output = new DataOutputStream(otheruser.getSocket().getOutputStream());
            //��ÿ���û��˷�������
                output.writeUTF(online.toString());
        	}catch(IOException ex){
        		System.err.println(ex);
        	}
        	}
        //�¿�һ���̣߳�������ǰ�����û���socket��������̣߳����߳����ڸ��������socket������
        HandleAClient task = new HandleAClient(socket);
        new Thread(task).start();

      }
    }catch(IOException ex) {
      System.err.println(ex);
    }
  }

  // �߳���
  class HandleAClient implements Runnable {
    private Socket socket; //�����ӵ�cocket

    public HandleAClient(Socket socket) {
      this.socket = socket;
    }

    public void run() {
    	
      try {
        // ��ȡ���̼߳�����socket�ͻ��˵�������
        DataInputStream inputFromClient = new DataInputStream(
          socket.getInputStream());

        // ѭ������
        while (true) {
          // ��ȡ�ͻ��˵�����
          String json = inputFromClient.readUTF();

          JSONObject data = JSONObject.fromObject(json.toString());
          //����ȡ������ת����ÿһ���û�
          for (int i = 0; i < clientList.size(); ) {
        	  try{
        		  //���������Ϣ���û��б�����json��ʽ���ݷ���ÿ���ͻ���
        		  JSONObject chat = new JSONObject();
                  chat.put("userlist", usernamelist);
                  chat.put("msg", data.getString("username")+" "+data.getString("time")+":\n"+data.getString("msg"));
                  User otheruser = clientList.get(i);
                  output = new DataOutputStream(otheruser.getSocket().getOutputStream());
                  output.writeUTF(chat.toString());
                  i++;
        	  }catch(IOException ex){
        		  //��������쳣��������ǰѭ���Ŀͻ���������
        		  //���б����Ƴ�
        		  User outuser = clientList.get(i);
        		  clientList.remove(i);
        		  //��ʾÿ���û����û�������
        		  usernamelist.remove(outuser.getUserName());
        		  JSONObject out = new JSONObject();
                  out.put("userlist", usernamelist);
                  out.put("msg", outuser.getUserName()+"������\n");
        		  //֪ͨ����
                  for (int j = 0; j < clientList.size(); j++) {
                	  try{
                      User otheruser = clientList.get(j);
                          output = new DataOutputStream(otheruser.getSocket().getOutputStream());
                          output.writeUTF(out.toString());
                	  }catch(IOException ex1){
                  	  }
                  }
          	}
          }
        }
      }
      catch(IOException e) {
        System.err.println(e);
      }
    }
  }
}
