package TCPMultiChatRoom;


import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class ChatClient extends JFrame {
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

// ������Ϣ��
  private JTextField jtf = new JTextField();
  
  private String username=null;

  // ��ʾ��Ϣ��
  private JTextArea jta = new JTextArea();
  private JTextArea userlist = new JTextArea(10,10);
  
  // IO 
  private DataOutputStream toServer;
  private DataInputStream fromServer;

  public ChatClient(String username) {
	System.out.println(username+"�ѵ�½");
    this.username=username;
    //���������
    final JPanel p = new JPanel();
    p.setLayout(new BorderLayout());
    p.add(new JLabel("�����������ݣ��س����ͣ�"), BorderLayout.WEST);
    p.add(jtf, BorderLayout.CENTER);
    jtf.setHorizontalAlignment(JTextField.LEFT);
    add(p, BorderLayout.SOUTH);
    //�ڰ�����м�����һ��������Ϣ��ʾ��
	add(new JScrollPane(jta), BorderLayout.CENTER);
	jta.setEditable(false);
	//�����û��б�
	final JPanel p2 = new JPanel();
	p2.setLayout(new BorderLayout());
	p2.setBorder(new TitledBorder("�û��б�"));
	p2.add(new JScrollPane(userlist), BorderLayout.CENTER);
	userlist.setEditable(false);
	add(p2, BorderLayout.EAST);
	jtf.addActionListener(new ButtonListener()); // Register listener
    setTitle("���������һ�ӭ�㣺"+username);
	setSize(500, 300);
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setVisible(true);

     JSONObject data =new JSONObject();
     data.put("username", username);
     data.put("msg", null);
    
    try {
      //����һ��socket���ӷ�����
      Socket socket = new Socket("127.0.0.1",8888);

      // ����һ�����������ڻ�ȡ������������
      fromServer = new DataInputStream(socket.getInputStream());

      // ����һ��������������������������
      toServer =new DataOutputStream(socket.getOutputStream());
      //��������������ݣ��û�����
      toServer.writeUTF(data.toString());
      //����һ���̣߳����ڶ�ȡ���������͹���������
      ReadThread readThread = new ReadThread();
      readThread.start();
		
    }
    catch (IOException ex) {
      jta.append("����������Ӧ");
    }
  }

  private class ButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      try {
        // �������ڸ�ʽ
    	  SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// �������ڸ�ʽ
          String time =df.format(new Date()).toString();
    	//��ȡ�������Ϣ
          String msg = jtf.getText().trim();
    	  if(msg.equals("")){}else{
    		  //������ݳ�json��ʽ
    	  JSONObject data =new JSONObject();
    	     data.put("username", username);
    	     data.put("msg", msg);
    	     data.put("time",time);
             //���������������
    	     toServer.writeUTF(data.toString());
             jtf.setText("");
    	  }
      }
      catch (Exception ex) {
        System.err.println(ex);
      }
    }
  }
  public class ReadThread extends Thread {

		public void run() {
			String json = null;
			try {
		        //����ѭ����������������������
				while (true) {
					//��ȡ������������
					json = fromServer.readUTF();
					//ת����json��ʽ
					JSONObject data = JSONObject.fromObject(json.toString());
					if(json != null){
						//��ӡ������Ϣ����ϵͳ��ʾ��Ϣ
						jta.append( data.getString("msg") + "\n");
						//ǿ��ʹ����ƶ���ײ�
						jta.selectAll();
						//ˢ���û��б�
						String list = "";
				        JSONArray jsonArray = data.getJSONArray("userlist");
				        for(int i=0;i<jsonArray.size();i++){
				        	list=list+jsonArray.get(i)+"\n";
				        }
						userlist.setText(list);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}