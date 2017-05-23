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
			showErrorMessage("�˿ںű���Ϊ������");
			return;
		}

		if (port < 1024 || port > 65535) {
			showErrorMessage("�˿ںű�����1024��65535֮��");
			return;
		}

		String name = nameTextField.getText().trim();

		if (name == null || name.equals("")) {
			showErrorMessage("���ֲ���Ϊ�գ�");
			return;
		}

		String ip = ipTextField.getText().trim();

		if (ip == null || ip.equals("")) {
			showErrorMessage("IP����Ϊ�գ�");
			return;
		}

		try {
			listModel.addElement("������");

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
			logMessage("�ͻ�������ʧ��");
			listModel.removeAllElements();
			e.printStackTrace();
			return;
		}

		logMessage("�ͻ������ӳɹ�");
		serviceUISetting(isConnected);
	}

	private void send() {
		if (!isConnected) {
			showErrorMessage("δ���ӵ���������");
			return;
		}
		String message = messageTextField.getText().trim();
		if (message == null || message.equals("")) {
			showErrorMessage("��Ϣ����Ϊ�գ�");
			return;
		}

		String to = sendTarget;
		try {
			sendMessage("MSG@" + to + "@" + me.description() + "@" + message);
			logMessage("��->" + to + ": " + message);
		} catch(Exception e) {
			e.printStackTrace();
			logMessage("������ʧ�ܣ���->" + to + ": " + message);
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
			messageToLabel.setText("To: ������");

			logMessage("�ѶϿ�����...");
		} catch(Exception e) {
			e.printStackTrace();
			isConnected = true;
			serviceUISetting(true);
			showErrorMessage("�������Ͽ�����ʧ�ܣ�");
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
		frame = new JFrame("�ͻ���");
		frame.setSize(600, 400);
		frame.setResizable(false);
		frame.setLayout(new BorderLayout());
		
		//setting panel
		ipTextField = new JTextField("127.0.0.1");
		portTextField = new JTextField("5555");
		nameTextField = new JTextField("");
		connectButton = new JButton("����");
		disconnectButton = new JButton("�Ͽ�");

		settingPanel = new JPanel();
		settingPanel.setLayout(new GridLayout(1, 8));
		settingPanel.add(new JLabel("         ����:"));
		settingPanel.add(nameTextField);
		settingPanel.add(new JLabel("������IP��ַ:"));
		settingPanel.add(ipTextField);
		settingPanel.add(new JLabel("�������˿ں�:"));
		settingPanel.add(portTextField);
		settingPanel.add(connectButton);
		settingPanel.add(disconnectButton);
		settingPanel.setBorder(new TitledBorder("�ͻ�������"));

		//user panel
		listModel = new DefaultListModel<String>();

		userList = new JList(listModel);
		userPanel = new JScrollPane(userList);
		userPanel.setBorder(new TitledBorder("�����û�"));

		//server log info
		messageTextArea = new JTextArea();
		messageTextArea.setEditable(false);
		messageTextArea.setForeground(Color.blue);

		messageBoxPanel = new JScrollPane(messageTextArea);
		messageBoxPanel.setBorder(new TitledBorder("������Ϣ"));

		//server message
		messageToLabel = new JLabel("To:������  "); 
		messageTextField = new JTextField();
		sendButton = new JButton("����");

		messagePanel = new JPanel(new BorderLayout());
		messagePanel.add(messageToLabel, "West");
		messagePanel.add(messageTextField, "Center");
		messagePanel.add(sendButton, "East");
		messagePanel.setBorder(new TitledBorder("������Ϣ"));

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
					messageToLabel.setText("To: ������");
				} else {
					String name = (String)listModel.getElementAt(index);
					if (onlineUsers.containsKey(name)) {
						sendTarget = onlineUsers.get(name).description();
						messageToLabel.setText("To: " + name);
					} else {
						sendTarget = "ALL";
						messageToLabel.setText("To: ������");
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
						logMessage("�������ѹرգ����ڶϿ�����...");
						disconnect();
						isRunning = false;
						return;
					} else if (command.equals("ERROR")) {
						String error = tokenizer.nextToken();
						logMessage("���������ش��󣬴������ͣ�" + error);
					} else if (command.equals("LOGIN")) {
						String status = tokenizer.nextToken();
						if (status.equals("SUCCESS")) {
							logMessage("��¼�ɹ���" + tokenizer.nextToken());
						} else if (status.equals("FAIL")) {
							logMessage("��¼ʧ�ܣ��Ͽ����ӣ�ԭ��" + tokenizer.nextToken());
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

							logMessage("���û���" + newUser.description() + "�����ߣ�");

						} else if (type.equals("DELETE")) {
							String userDescription = tokenizer.nextToken();
							User deleteUser = new User(userDescription);
							onlineUsers.remove(deleteUser.getName());
							listModel.removeElement(deleteUser.getName());

							logMessage("�û���" + deleteUser.description() + "�����ߣ�");

							if (sendTarget.equals(deleteUser.description())) {
								sendTarget = "ALL";
								messageToLabel.setText("To: ������");
							}

						} else if (type.equals("LIST")) {
							int num = Integer.parseInt(tokenizer.nextToken());
							for (int i = 0; i < num; i++) {
								String userDescription = tokenizer.nextToken();
								User newUser = new User(userDescription);
								onlineUsers.put(newUser.getName(), newUser);
								listModel.addElement(newUser.getName());

								logMessage("��ȡ���û���" + newUser.description() + "�����ߣ�");
							}
						}
					} else if (command.equals("MSG")) {
						StringBuffer buffer = new StringBuffer();
						String to = tokenizer.nextToken();
						String from = tokenizer.nextToken();
						String content = tokenizer.nextToken();

						buffer.append(from);
						if (to.equals("ALL")) {
							buffer.append("��Ⱥ����");
						}
						buffer.append(": " + content);
						logMessage(buffer.toString());
					}

				} catch(Exception e) {
					e.printStackTrace();
					logMessage("������Ϣ�쳣��");
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

