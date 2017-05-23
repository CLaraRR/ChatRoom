package TCPChatRoom;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class ChatClient extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JLabel desIPLabel;
	private JLabel desPortLabel;
	private JLabel clientNameLabel;
	private JLabel localPortLabel;
	private JButton bindButton;
	private JTextField desIP;
	private JTextField desPort;
	private JTextField clientName;
	private JTextField localPort;
    JTextArea chatArea;
	private JTextArea sendText;
	private JButton sendButton;
	
	private int localport;
	private String ip;
	private String destip;
	private int destport;
	
	private Socket socketConn;// 声明Socket连接
	private PrintWriter out ;
	private BufferedReader in ;
	
	private Receiver receiveThread;
	
	
	SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm:ss");
	

	/**
	 * Create the frame.
	 */
	public ChatClient() {
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.setBounds(100, 100, 547, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		//setContentPane(contentPane);
		contentPane.setLayout(null);
		
		desIPLabel = new JLabel("服务器IP：");
		desIPLabel.setBounds(20, 10, 64, 15);
		contentPane.add(desIPLabel);
		
		desIP = new JTextField();
		desIP.setBounds(91, 7, 116, 21);
		contentPane.add(desIP);
		desIP.setColumns(10);
		
		desPortLabel = new JLabel("端口号：");
		desPortLabel.setBounds(237, 10, 67, 15);
		contentPane.add(desPortLabel);
		
		desPort = new JTextField();
		desPort.setBounds(292, 7, 100, 21);
		contentPane.add(desPort);
		desPort.setColumns(10);
		
		clientNameLabel = new JLabel("我的名字：");
		clientNameLabel.setBounds(20, 35, 69, 15);
		contentPane.add(clientNameLabel);
		
		clientName = new JTextField();
		clientName.setBounds(91, 34, 116, 21);
		contentPane.add(clientName);
		clientName.setColumns(10);
		
		bindButton = new JButton("连接服务器");
		bindButton.addActionListener(this);
		bindButton.setBounds(398, 31, 116, 23);
		contentPane.add(bindButton);
		
		localPortLabel = new JLabel("本机端口号：");
		localPortLabel.setBounds(223, 35, 80, 15);
		contentPane.add(localPortLabel);
		
		localPort = new JTextField();
		localPort.setBounds(292, 32, 100, 21);
		contentPane.add(localPort);
		localPort.setColumns(10);
		
		chatArea = new JTextArea();
		chatArea.setRows(20);
		chatArea.setEditable(false);
		chatArea.setBounds(24, 69, 494, 218);
		contentPane.add(chatArea);
		
		sendText = new JTextArea();
		sendText.setBounds(20, 291, 420, 60);
		contentPane.add(sendText);
		
		sendButton = new JButton("发送");
		sendButton.addActionListener(this);
		sendButton.setBounds(449, 316, 65, 23);
		contentPane.add(sendButton);
		
		getContentPane().add(contentPane); // 将面板添加到窗体
		this.setVisible(true);
		//添加关闭窗口事件
		this.addWindowListener(new WindowAdapter(){
		    public void windowClosing(WindowEvent event)
		    {
		    	try {
					socketConn.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}// 关闭资源
				System.exit(0);
		    }
		});
		
	}
	


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getActionCommand().equals("发送")){
			if(localport<=0){
				System.out.println("请先绑定端口!");
				return;
			}
			
			
			Date date=new Date();
			String content=clientName.getText()+" "+dateformat.format(date)+"\n";
			content+=sendText.getText();
			System.out.print(content+"\n");
			out.println(content+"\n");// 发送到服务端
			//chatArea.setText(chatArea.getText()+"\n"+content+"\n");
			//清空聊天框
			sendText.setText(null);
		}else if(e.getActionCommand().equals("连接服务器")){
			this.setTitle(clientName.getText()+"的聊天客户端");
			//获取对方IP
			destip=desIP.getText();
			//获取对方端口号
			destport=Integer.parseInt(desPort.getText());
			//获取自己端口号
			this.localport=Integer.parseInt(localPort.getText());
			connectSocket();
			receiveThread=new Receiver();
			receiveThread.start();
			
			
		}
	}



	private void connectSocket() {
		// TODO Auto-generated method stub
		try {
            
            if (destip.equals("localhost") || destip.equals("127.0.0.1")) {// 判断IP地址(域名)如果是本机localhost
                socketConn = new Socket(InetAddress.getLocalHost(), destport);// 创建本地连接
            } else {
                socketConn = new Socket(InetAddress.getByName(destip), destport);// 创建远程连接
            }
            //BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));// 获得从键盘输入的流
            out = new PrintWriter(socketConn.getOutputStream(), true);// 获得服务器写内容的数据流
            in = new BufferedReader(new InputStreamReader(socketConn.getInputStream()));// 获得接收服务器发送内容的缓冲流
            
            
        } catch (SecurityException e) {// 捕获安全性错误时引发的异常
            System.out.println("连接服务器出现安全问题！");
        } catch (IOException e) {// 捕获IO流异常
            System.out.println("连接服务器出现I/O错误！");
        }
	}
	private class Receiver extends Thread{
		public void run(){
			while(true){
				try {
					String info = in.readLine();//从服务器端读取数据
					if(info.length()>0){
						chatArea.setText(chatArea.getText()+info+"\n");
						System.out.println("收到服务端消息："+info);
					}
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}// 从服务器读取字符串
				
			}
		}
	}
	
	public static void main(String[] args){
		new ChatClient();
	}


}
