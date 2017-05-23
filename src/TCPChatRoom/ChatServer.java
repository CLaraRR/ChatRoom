package TCPChatRoom;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;


public class ChatServer extends JFrame implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JLabel serverNameLabel;
	private JLabel portLabel;
	private JButton bindButton;
	private JTextField serverName;
	private JTextField serverPort;
    JTextArea chatArea;
	private JTextArea sendText;
	private JButton sendButton;
	
	//Socket
	private ServerSocket serverSocket;
	int port;
	Vector<ClientServiceThread> clientServiceThreads;
	ServerThread serverThread;


	/**
	 * Create the frame.
	 */
	public ChatServer() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.setBounds(100, 100, 547, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		//setContentPane(contentPane);
		contentPane.setLayout(null);
		
		serverNameLabel = new JLabel("���������ƣ�");
		serverNameLabel.setBounds(20, 10, 96, 15);
		contentPane.add(serverNameLabel);
		
		serverName = new JTextField("TCP������");
		serverName.setBounds(101, 7, 116, 21);
		contentPane.add(serverName);
		serverName.setColumns(10);
		serverName.setEditable(false);
		
		portLabel = new JLabel("�˿ںţ�");
		portLabel.setBounds(45, 35, 67, 15);
		contentPane.add(portLabel);
		
		serverPort = new JTextField();
		serverPort.setBounds(101, 32, 116, 21);
		contentPane.add(serverPort);
		serverPort.setColumns(10);
		
		bindButton = new JButton("��������");
		bindButton.addActionListener(this);
		bindButton.setBounds(282, 31, 116, 23);
		contentPane.add(bindButton);
		
		chatArea = new JTextArea();
		chatArea.setRows(20);
		chatArea.setEditable(false);
		chatArea.setBounds(24, 69, 494, 218);
		contentPane.add(chatArea);
		
		sendText = new JTextArea();
		sendText.setBounds(20, 291, 420, 60);
		contentPane.add(sendText);
		
		sendButton = new JButton("����");
		sendButton.addActionListener(this);
		sendButton.setBounds(449, 316, 65, 23);
		contentPane.add(sendButton);
		
		getContentPane().add(contentPane); // �������ӵ�����
		this.setVisible(true);
		//��ӹرմ����¼�
		this.addWindowListener(new WindowAdapter(){
		    public void windowClosing(WindowEvent event)
		    {
		    	try {
					serverThread.closeThread();
					for(ClientServiceThread clientthread:clientServiceThreads){
						clientthread.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.exit(0);
		    }
		});
		
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getActionCommand().equals("��������")){
			startServer();
			
		}else if(e.getActionCommand().equals("����")){
			
			
		}
	}
	
	private void startServer() {
		// TODO Auto-generated method stub
		port=Integer.parseInt(serverPort.getText());
		try {
			clientServiceThreads = new Vector<ClientServiceThread>();
			serverSocket = new ServerSocket(port);
			serverThread = new ServerThread();//����һ�����������߳��������ͻ�����������
			serverThread.start();
			
		} catch (BindException e) {
			String msg="����������ʧ�ܣ��˿ڱ�ռ�ã�";
			System.out.println(msg);
			chatArea.setText(msg);
			return;
		} catch (Exception e) {
			String msg="����������ʧ�ܣ������쳣��";
			System.out.println(msg);
			chatArea.setText(msg);
			e.printStackTrace();
			return;
		}

		//logMessage("�������������������ޣ�" + maxClientNum + " �˿ںţ�" + port);
	}

	//���������̣߳�����������������Ȼ��Ϊÿ���û����ӷ���һ�����߳�
	private class ServerThread extends Thread {
		private boolean isRunning;

		public ServerThread() {
			this.isRunning = true;
		}

		public void run() {
			while (this.isRunning) {
				//System.out.println("server recieving connection request!");
				try {
					if (!serverSocket.isClosed()) {
						Socket socket = serverSocket.accept();
						ClientServiceThread clientServiceThread = new ClientServiceThread(socket);
						clientServiceThreads.add(clientServiceThread);
						chatArea.setText(chatArea.getText()+socket.getInetAddress().toString()+"������...\n");
						clientServiceThread.start();
						
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}

		public synchronized void closeThread() throws IOException {
			this.isRunning = false;
			serverSocket.close();
			System.out.println("serverSocket close!!!");
		}
	}
	
	//�����ÿ���û��Ĵ��̣߳���������ÿ���û�������
		private class ClientServiceThread extends Thread {
			private Socket socket;
			private BufferedReader reader;
			private PrintWriter writer;
			private boolean isRunning;

			private synchronized boolean init() {
				try {
					reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					writer = new PrintWriter(socket.getOutputStream());
					return true;

				} catch(Exception e) {
					e.printStackTrace();
					return false;
				}
			}

			public ClientServiceThread(Socket socket) {
				this.socket = socket;
				this.isRunning = init();
				if (!this.isRunning) {
					String msg="���߳̿���ʧ��";
					chatArea.setText(chatArea.getText()+msg);
					System.out.println(msg);
				}
			}

			public void run() {
				while (isRunning) {
					try {
						String message = reader.readLine();
						if(message.length()>0){
							System.out.println("�յ��ͻ�����Ϣ��" + message);
							chatArea.setText(chatArea.getText()+message+"\n");
							dispatchMessage(message+"\n");
						}
						
						
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}

			public void dispatchMessage(String message) {
				
				//send to everyone
				for(ClientServiceThread clientthread:clientServiceThreads){
					clientthread.sendMessage(message);
				}
				
			}

			public void close() throws IOException {
				this.isRunning = false;
				this.reader.close();
				this.writer.close();
				this.socket.close();
				
			}

			public void sendMessage(String message) {
				writer.println(message);
				writer.flush();
			}


		}
		
		public static void main(String[] args){
			new ChatServer();
		}

}
