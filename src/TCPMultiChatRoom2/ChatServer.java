package TCPMultiChatRoom2;

import javax.swing.*;
import javax.swing.border.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

/*
	message type
	1.alias:
		USER <= USER_NAME%USER_IPADDR

	2.format:
	server:	
			MSG		@	to 		@	from 	@	content	
	 		 			ALL 		SERVER 		xxx
						ALL 		USER		xxx
						USER 		USER 		xxx

			LOGIN 	@ 	status 	@ 	content
						SUCCESS 	xxx
						FAIL 		xxx

			USER 	@ 	type 	@	other
						ADD 		USER
						DELETE 		USER
						LIST 		number 	{@	USER}+

			ERROR 	@ 	TYPE

			CLOSE
	
	client:
			MSG 	@	to 		@	from	@	content
						ALL 		USER 		xxx
						USER 		USER 		xxx
			LOGOUT

			LOGIN 	@ 	USER 	

*/


public class ChatServer {
	//UI
	private JFrame frame;
	private JPanel settingPanel, messagePanel;
	private JSplitPane centerSplitPanel;
	private JScrollPane userPanel, logPanel;
	private JTextArea logTextArea;
	private JTextField maxClientTextField, portTextField, serverMessageTextField;
	private JButton startButton, stopButton, sendButton;
	private JList userList;

	//Model
	private DefaultListModel<String> listModel;

	//Socket
	private ServerSocket serverSocket;

	//Status
	private boolean isStart = false;
	private int maxClientNum;

	//Threads
	//ArrayList<ClientServiceThread> clientServiceThreads;
	ConcurrentHashMap<String, ClientServiceThread> clientServiceThreads;
	ServerThread serverThread;
	
	public ChatServer() {
		initUI();
	}

	private void startServer() {
		int port;

		try {
			port = Integer.parseInt(portTextField.getText().trim());
		} catch(NumberFormatException e) {
			showErrorMessage("端口号必须为整数！");
			return;
		}

		if (port < 1024 || port > 65535) {
			showErrorMessage("端口号必须在1024～65535之间");
			return;
		}

		try {
			maxClientNum = Integer.parseInt(maxClientTextField.getText().trim());
		} catch(NumberFormatException e) {
			showErrorMessage("人数上限必须是正整数！");
			maxClientNum = 0;
			return;
		}

		if (maxClientNum <= 0) {
			showErrorMessage("人数上限必须是正整数！");
			maxClientNum = 0;
			return;
		}

		try {
			clientServiceThreads = new ConcurrentHashMap<String, ClientServiceThread>();
			serverSocket = new ServerSocket(port);
			serverThread = new ServerThread();
			serverThread.start();
			isStart = true;
		} catch (BindException e) {
			isStart = false;
			showErrorMessage("启动服务器失败：端口被占用！");
			return;
		} catch (Exception e) {
			isStart = false;
			showErrorMessage("启动服务器失败：启动异常！");
			e.printStackTrace();
			return;
		}

		logMessage("服务器启动：人数上限：" + maxClientNum + " 端口号：" + port);
		serviceUISetting(true);
	}

	private synchronized void stopServer() {
		try {
			serverThread.closeThread();

			for (Map.Entry<String, ClientServiceThread> entry : clientServiceThreads.entrySet()) {
				ClientServiceThread clientThread = entry.getValue();
				clientThread.sendMessage("CLOSE");
				clientThread.close();
			}

			clientServiceThreads.clear();
			listModel.removeAllElements();
			isStart = false;
			serviceUISetting(false);
			logMessage("服务器已关闭！");
		} catch(Exception e) {
			e.printStackTrace();
			showErrorMessage("关闭服务器异常！");
			isStart = true;
			serviceUISetting(true);
		}
	}

	private void sendAll() {
		if (!isStart) {
			showErrorMessage("服务器还未启动，不能发送消息！");
			return;
		}

		if (clientServiceThreads.size() == 0) {
			showErrorMessage("没有用户在线，不能发送消息！");
			return;
		}

		String message = serverMessageTextField.getText().trim();
		if (message == null || message.equals("")) {
			showErrorMessage("发送消息不能为空！");
			return;
		}

		for (Map.Entry<String, ClientServiceThread> entry : clientServiceThreads.entrySet()) {
			entry.getValue().sendMessage("MSG@ALL@SERVER@" + message);
		}

		logMessage("Server: " + message);
		serverMessageTextField.setText(null);
	}

	private void logMessage(String msg) {
		logTextArea.append(msg + "\r\n");
	}
	
	private void showErrorMessage(String msg) {
		JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
	}


	//UI Settings!
	private void initUI() {
		frame = new JFrame("服务器");
		frame.setSize(600, 400);
		frame.setResizable(false);
		frame.setLayout(new BorderLayout());
		
		//setting panel
		maxClientTextField = new JTextField("10");
		portTextField = new JTextField("5555");
		startButton = new JButton("启动");
		stopButton = new JButton("停止");

		settingPanel = new JPanel();
		settingPanel.setLayout(new GridLayout(1, 6));
		settingPanel.add(new JLabel("人数上限"));
		settingPanel.add(maxClientTextField);
		settingPanel.add(new JLabel("端口号"));
		settingPanel.add(portTextField);
		settingPanel.add(startButton);
		settingPanel.add(stopButton);
		settingPanel.setBorder(new TitledBorder("服务器配置"));

		//user panel
		listModel = new DefaultListModel<String>();

		userList = new JList(listModel);
		userPanel = new JScrollPane(userList);
		userPanel.setBorder(new TitledBorder("在线用户"));

		//server log info
		logTextArea = new JTextArea();
		logTextArea.setEditable(false);
		logTextArea.setForeground(Color.blue);

		logPanel = new JScrollPane(logTextArea);
		logPanel.setBorder(new TitledBorder("服务器日志"));

		//server message
		serverMessageTextField = new JTextField();
		sendButton = new JButton("发送");

		messagePanel = new JPanel(new BorderLayout());
		messagePanel.add(serverMessageTextField, "Center");
		messagePanel.add(sendButton, "East");
		messagePanel.setBorder(new TitledBorder("广播消息"));


		//add to frame
		centerSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, userPanel, logPanel);
		centerSplitPanel.setDividerLocation(100);

		frame.add(settingPanel, "North");
		frame.add(centerSplitPanel, "Center");
		frame.add(messagePanel, "South");
		frame.setVisible(true);

		addActionListenersToUI();

		serviceUISetting(false);
	}

	private void serviceUISetting(boolean started) {
		maxClientTextField.setEnabled(!started);
		portTextField.setEnabled(!started);
		startButton.setEnabled(!started);
		stopButton.setEnabled(started);
		serverMessageTextField.setEnabled(started);
		sendButton.setEnabled(started);
	}

	private void addActionListenersToUI() {
		serverMessageTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendAll();
			}
		});

		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendAll();
			}
		});

		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isStart) {
					startServer();
				}
			}
		});

		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (isStart) {
					stopServer();
				}
			}
		});

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (isStart) {
					stopServer();
				}
				System.exit(0);
			}
		});
	}
	
	//Server Thread class
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

						if (clientServiceThreads.size() == maxClientNum) {
							PrintWriter writer = new PrintWriter(socket.getOutputStream());
							writer.println("LOGIN@FAIL@对不起，服务器在线人数已达到上限，请稍候尝试！");
							writer.flush();
							writer.close();
							socket.close();
						} else {
							ClientServiceThread clientServiceThread = new ClientServiceThread(socket);
							User user = clientServiceThread.getUser();
							clientServiceThreads.put(user.description(), clientServiceThread);
							listModel.addElement(user.getName());
							logMessage(user.description() + "上线...");

							clientServiceThread.start();
						}
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

	//Client Thread class
	private class ClientServiceThread extends Thread {
		private Socket socket;
		private User user;
		private BufferedReader reader;
		private PrintWriter writer;
		private boolean isRunning;

		private synchronized boolean init() {
			try {
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer = new PrintWriter(socket.getOutputStream());

				String info = reader.readLine();
				StringTokenizer tokenizer = new StringTokenizer(info, "@");
				String type = tokenizer.nextToken();
				if (!type.equals("LOGIN")) {
					sendMessage("ERROR@MESSAGE_TYPE");
					return false;
				}

				user = new User(tokenizer.nextToken());
				sendMessage("LOGIN@SUCCESS@" + user.description() + "与服务器连接成功！");

				int clientNum = clientServiceThreads.size();
				if (clientNum > 0) {
					//tell this client who else are online
					StringBuffer buffer = new StringBuffer();
					buffer.append("@");
					for (Map.Entry<String, ClientServiceThread> entry : clientServiceThreads.entrySet()) {
						ClientServiceThread serviceThread = entry.getValue();
						buffer.append(serviceThread.getUser().description() + "@");
						//tell other users that this user is online
						serviceThread.sendMessage("USER@ADD@" + user.description());
					}

					sendMessage("USER@LIST@" + clientNum + buffer.toString());
				}

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
				logMessage("服务线程开启失败！");
			}
		}

		public void run() {
			while (isRunning) {
				try {
					String message = reader.readLine();
					System.out.println("recieve message: " + message);
					if (message.equals("LOGOUT")) {
						logMessage(user.description() + "下线...");

						int clientNum = clientServiceThreads.size();
						
						//tell other users that this user is offline
						for (Map.Entry<String, ClientServiceThread> entry : clientServiceThreads.entrySet()) {
							entry.getValue().sendMessage("USER@DELETE@" + user.description());
						}

						//remove this user and service thread
						listModel.removeElement(user.getName());
						clientServiceThreads.remove(user.description());

						System.out.println(user.description() + " logout, now " + listModel.size() + " client(s) online...(" + clientServiceThreads.size() + " Thread(s))");

						close();
						return;
					} else {
						dispatchMessage(message);
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void dispatchMessage(String message) {
			StringTokenizer tokenizer = new StringTokenizer(message, "@");
			String type = tokenizer.nextToken();
			if (!type.equals("MSG")) {
				sendMessage("ERROR@MESSAGE_TYPE");
				return;
			}

			String to = tokenizer.nextToken();
			String from = tokenizer.nextToken();
			String content = tokenizer.nextToken();

			logMessage(from + "->" + to + ": " + content);
			if (to.equals("ALL")) {
				//send to everyone
				for (Map.Entry<String, ClientServiceThread> entry : clientServiceThreads.entrySet()) {
					entry.getValue().sendMessage(message);
				}
			} else {
				//send to some one
				if (clientServiceThreads.containsKey(to)) {
					clientServiceThreads.get(to).sendMessage(message);
				} else {
					sendMessage("ERROR@INVALID_USER");
				}
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

		public User getUser() {
			return user;
		}
	}
	
	public static void main(String args[]) {
		new ChatServer();
	}
}

