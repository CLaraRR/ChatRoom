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

// 接受消息框
  private JTextField jtf = new JTextField();
  
  private String username=null;

  // 显示信息框
  private JTextArea jta = new JTextArea();
  private JTextArea userlist = new JTextArea(10,10);
  
  // IO 
  private DataOutputStream toServer;
  private DataInputStream fromServer;

  public ChatClient(String username) {
	System.out.println(username+"已登陆");
    this.username=username;
    //设置输入框
    final JPanel p = new JPanel();
    p.setLayout(new BorderLayout());
    p.add(new JLabel("输入聊天内容（回车发送）"), BorderLayout.WEST);
    p.add(jtf, BorderLayout.CENTER);
    jtf.setHorizontalAlignment(JTextField.LEFT);
    add(p, BorderLayout.SOUTH);
    //在版面的中间增加一个聊天信息显示框
	add(new JScrollPane(jta), BorderLayout.CENTER);
	jta.setEditable(false);
	//设置用户列表
	final JPanel p2 = new JPanel();
	p2.setLayout(new BorderLayout());
	p2.setBorder(new TitledBorder("用户列表"));
	p2.add(new JScrollPane(userlist), BorderLayout.CENTER);
	userlist.setEditable(false);
	add(p2, BorderLayout.EAST);
	jtf.addActionListener(new ButtonListener()); // Register listener
    setTitle("多人聊天室欢迎你："+username);
	setSize(500, 300);
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setVisible(true);

     JSONObject data =new JSONObject();
     data.put("username", username);
     data.put("msg", null);
    
    try {
      //创建一个socket链接服务器
      Socket socket = new Socket("127.0.0.1",8888);

      // 创建一个输入流用于获取服务器的数据
      fromServer = new DataInputStream(socket.getInputStream());

      // 创建一个输出流用于向服务器发送数据
      toServer =new DataOutputStream(socket.getOutputStream());
      //向服务器发送数据（用户名）
      toServer.writeUTF(data.toString());
      //开启一个线程，用于读取服务器发送过来的数据
      ReadThread readThread = new ReadThread();
      readThread.start();
		
    }
    catch (IOException ex) {
      jta.append("服务器无响应");
    }
  }

  private class ButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      try {
        // 设置日期格式
    	  SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
          String time =df.format(new Date()).toString();
    	//获取输入框信息
          String msg = jtf.getText().trim();
    	  if(msg.equals("")){}else{
    		  //打包数据成json格式
    	  JSONObject data =new JSONObject();
    	     data.put("username", username);
    	     data.put("msg", msg);
    	     data.put("time",time);
             //向服务器发送数据
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
		        //无线循环监听服务器发来的数据
				while (true) {
					//读取服务器的数据
					json = fromServer.readUTF();
					//转化成json格式
					JSONObject data = JSONObject.fromObject(json.toString());
					if(json != null){
						//打印聊天信息或者系统提示信息
						jta.append( data.getString("msg") + "\n");
						//强制使光标移动最底部
						jta.selectAll();
						//刷新用户列表
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