package TCPMultiChatRoom2;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

public class ChatClient {
	//UI
	private JFrame frame;
	private JPanel settingPanel, messagePanel;
	private JSplitPane centerSplitPanel;
	private JScrollPane userPanel, messageBoxPanel;
	private JTextArea messageTextArea;
	private JTextField nameTextField, ipTextField, portTextField, messageTextField;
	private JLabel messageToLabel;
	private JButton connectButton, disconnectButton, sendButton;
	private JList userList;

	//Model
	private DefaultListModel<String> listModel;
	private User me;
	private ConcurrentHashMap<String, User> onlineUsers = new ConcurrentHashMap<String, User>();
	private String sendTarget = "ALL";

	//Socket
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;

	//Thread
	private MessageThread messageThread;

	//Status
	private boolean isConnected;

	public ChatClient() {
		initUI();
	}

	private void connect() {
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

		String name = nameTextField.getText().trim();

		if (name == null || name.equals("")) {
			showErrorMessage("名字不能为空！");
			return;
		}

		String ip = ipTextField.getText().trim();

		if (ip == null || ip.equals("")) {
			showErrorMessage("IP不能为空！");
			return;
		}

		try {
			listModel.addElement("所有人");

			me = new User(name, ip);
			socket = new Socket(ip, port);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream());

			String myIP = socket.getLocalAddress().toString().substring(1);
			//System.out.println(myIP);
			sendMessage("LOGIN@" + name + "%" + myIP);

			messageThread = new MessageThread();
			messageThread.start();
			isConnected = true;

		} catch(Exception e) {
			isConnected = false;
			logMessage("客户端连接失败");
			listModel.removeAllElements();
			e.printStackTrace();
			return;
		}

		logMessage("客户端连接成功");
		serviceUISetting(isConnected);
	}

	private void send() {
		if (!isConnected) {
			showErrorMessage("未连接到服务器！");
			return;
		}
		String message = messageTextField.getText().trim();
		if (message == null || message.equals("")) {
			showErrorMessage("消息不能为空！");
			return;
		}

		String to = sendTarget;
		try {
			sendMessage("MSG@" + to + "@" + me.description() + "@" + message);
			logMessage("我->" + to + ": " + message);
		} catch(Exception e) {
			e.printStackTrace();
			logMessage("（发送失败）我->" + to + ": " + message);
		}

		messageTextField.setText(null);
	}

	private synchronized void disconnect() {
		try {
			sendMessage("LOGOUT");

			messageThread.close();
			listModel.removeAllElements();
			onlineUsers.clear();

			reader.close();
			writer.close();
			socket.close();
			isConnected = false;
			serviceUISetting(false);

			sendTarget = "ALL";
			messageToLabel.setText("To: 所有人");

			logMessage("已断开连接...");
		} catch(Exception e) {
			e.printStackTrace();
			isConnected = true;
			serviceUISetting(true);
			showErrorMessage("服务器断开连接失败！");
		}
	}

	private void sendMessage(String message) {
		writer.println(message);
		writer.flush();
	}

	private void logMessage(String msg) {
		messageTextArea.append(msg + "\r\n");
	}

	private void showErrorMessage(String msg) {
		JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
	}

	private void initUI() {
		frame = new JFrame("客户端");
		frame.setSize(600, 400);
		frame.setResizable(false);
		frame.setLayout(new BorderLayout());
		
		//setting panel
		ipTextField = new JTextField("127.0.0.1");
		portTextField = new JTextField("5555");
		nameTextField = new JTextField("");
		connectButton = new JButton("连接");
		disconnectButton = new JButton("断开");

		settingPanel = new JPanel();
		settingPanel.setLayout(new GridLayout(1, 8));
		settingPanel.add(new JLabel("         名字:"));
		settingPanel.add(nameTextField);
		settingPanel.add(new JLabel("服务器IP地址:"));
		settingPanel.add(ipTextField);
		settingPanel.add(new JLabel("服务器端口号:"));
		settingPanel.add(portTextField);
		settingPanel.add(connectButton);
		settingPanel.add(disconnectButton);
		settingPanel.setBorder(new TitledBorder("客户端配置"));

		//user panel
		listModel = new DefaultListModel<String>();

		userList = new JList(listModel);
		userPanel = new JScrollPane(userList);
		userPanel.setBorder(new TitledBorder("在线用户"));

		//server log info
		messageTextArea = new JTextArea();
		messageTextArea.setEditable(false);
		messageTextArea.setForeground(Color.blue);

		messageBoxPanel = new JScrollPane(messageTextArea);
		messageBoxPanel.setBorder(new TitledBorder("接收消息"));

		//server message
		messageToLabel = new JLabel("To:所有人  "); 
		messageTextField = new JTextField();
		sendButton = new JButton("发送");

		messagePanel = new JPanel(new BorderLayout());
		messagePanel.add(messageToLabel, "West");
		messagePanel.add(messageTextField, "Center");
		messagePanel.add(sendButton, "East");
		messagePanel.setBorder(new TitledBorder("发送消息"));

		//add to frame
		centerSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, userPanel, messageBoxPanel);
		centerSplitPanel.setDividerLocation(100);

		frame.add(settingPanel, "North");
		frame.add(centerSplitPanel, "Center");
		frame.add(messagePanel, "South");
		frame.setVisible(true);

		addActionListenersToUI();

		serviceUISetting(false);
	}

	private void addActionListenersToUI() {
		messageTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});

		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});

		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isConnected) {
					connect();
				}
			}
		});

		disconnectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (isConnected) {
					disconnect();
				}
			}
		});

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (isConnected) {
					disconnect();
				}
				System.exit(0);
			}
		});

		userList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int index = userList.getSelectedIndex();
				if (index < 0) return;

				//System.out.print(index + ": ");
				if (index == 0) {
					sendTarget = "ALL";
					messageToLabel.setText("To: 所有人");
				} else {
					String name = (String)listModel.getElementAt(index);
					if (onlineUsers.containsKey(name)) {
						sendTarget = onlineUsers.get(name).description();
						messageToLabel.setText("To: " + name);
					} else {
						sendTarget = "ALL";
						messageToLabel.setText("To: 所有人");
					}
				}
				//System.out.println(sendTarget);
			}
		});
	}

	private void serviceUISetting(boolean connected) {
		nameTextField.setEnabled(!connected);
		ipTextField.setEnabled(!connected);
		portTextField.setEnabled(!connected);
		connectButton.setEnabled(!connected);
		disconnectButton.setEnabled(connected);
		messageTextField.setEnabled(connected);
		sendButton.setEnabled(connected);
	}

	private class MessageThread extends Thread {
		private boolean isRunning = false;

		public MessageThread() {
			isRunning = true;
		}

		public void run() {
			while (isRunning) {
				try {
					String message = reader.readLine();
					StringTokenizer tokenizer = new StringTokenizer(message, "@");
					String command = tokenizer.nextToken();

					if (command.equals("CLOSE")) {
						logMessage("服务器已关闭，正在断开连接...");
						disconnect();
						isRunning = false;
						return;
					} else if (command.equals("ERROR")) {
						String error = tokenizer.nextToken();
						logMessage("服务器返回错误，错误类型：" + error);
					} else if (command.equals("LOGIN")) {
						String status = tokenizer.nextToken();
						if (status.equals("SUCCESS")) {
							logMessage("登录成功！" + tokenizer.nextToken());
						} else if (status.equals("FAIL")) {
							logMessage("登录失败，断开连接！原因：" + tokenizer.nextToken());
							disconnect();
							isRunning = false;
							return;
						}
					} else if (command.equals("USER")) {
						String type = tokenizer.nextToken();
						if (type.equals("ADD")) {
							String userDescription = tokenizer.nextToken();
							User newUser = new User(userDescription);
							onlineUsers.put(newUser.getName(), newUser);
							listModel.addElement(newUser.getName());

							logMessage("新用户（" + newUser.description() + "）上线！");

						} else if (type.equals("DELETE")) {
							String userDescription = tokenizer.nextToken();
							User deleteUser = new User(userDescription);
							onlineUsers.remove(deleteUser.getName());
							listModel.removeElement(deleteUser.getName());

							logMessage("用户（" + deleteUser.description() + "）下线！");

							if (sendTarget.equals(deleteUser.description())) {
								sendTarget = "ALL";
								messageToLabel.setText("To: 所有人");
							}

						} else if (type.equals("LIST")) {
							int num = Integer.parseInt(tokenizer.nextToken());
							for (int i = 0; i < num; i++) {
								String userDescription = tokenizer.nextToken();
								User newUser = new User(userDescription);
								onlineUsers.put(newUser.getName(), newUser);
								listModel.addElement(newUser.getName());

								logMessage("获取到用户（" + newUser.description() + "）在线！");
							}
						}
					} else if (command.equals("MSG")) {
						StringBuffer buffer = new StringBuffer();
						String to = tokenizer.nextToken();
						String from = tokenizer.nextToken();
						String content = tokenizer.nextToken();

						buffer.append(from);
						if (to.equals("ALL")) {
							buffer.append("（群发）");
						}
						buffer.append(": " + content);
						logMessage(buffer.toString());
					}

				} catch(Exception e) {
					e.printStackTrace();
					logMessage("接收消息异常！");
				}
			}
		}

		public void close() {
			isRunning = false;
		}
	}

	public static void main(String[] args) {
		new ChatClient();
	}
}

