package UDPChatRoom;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
	

	private Sender senderThread;
	private Receiver receiverThread;//�����߳�
	SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm:ss");
	int localport;

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
		
		desIPLabel = new JLabel("�Է�IP��");
		desIPLabel.setBounds(20, 10, 54, 15);
		contentPane.add(desIPLabel);
		
		desIP = new JTextField();
		desIP.setBounds(72, 7, 116, 21);
		contentPane.add(desIP);
		desIP.setColumns(10);
		
		desPortLabel = new JLabel("\u5BF9\u65B9\u7AEF\u53E3\u53F7\uFF1A");
		desPortLabel.setBounds(238, 10, 87, 15);
		contentPane.add(desPortLabel);
		
		desPort = new JTextField();
		desPort.setBounds(319, 7, 100, 21);
		contentPane.add(desPort);
		desPort.setColumns(10);
		
		clientNameLabel = new JLabel("�û�����");
		clientNameLabel.setBounds(20, 35, 54, 15);
		contentPane.add(clientNameLabel);
		
		clientName = new JTextField();
		clientName.setBounds(72, 34, 116, 21);
		contentPane.add(clientName);
		clientName.setColumns(10);
		
		bindButton = new JButton("ȷ��");
		bindButton.addActionListener(this);
		bindButton.setBounds(449, 31, 65, 23);
		contentPane.add(bindButton);
		
		localPortLabel = new JLabel("�����˿ںţ�");
		localPortLabel.setBounds(238, 35, 80, 15);
		contentPane.add(localPortLabel);
		
		localPort = new JTextField();
		localPort.setBounds(319, 32, 100, 21);
		contentPane.add(localPort);
		localPort.setColumns(10);
		
		chatArea = new JTextArea();
		chatArea.setRows(20);
		chatArea.setEditable(false);
		chatArea.setBounds(20, 63, 494, 218);
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
				System.exit(0);
		    }
		});
		
	}
	


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getActionCommand().equals("����")){
			if(localport<=0){
				System.out.println("���Ȱ󶨶˿�!");
				return;
			}
			//����IP
			String localip="127.0.0.1";
			//��ȡ�Է�IP
			String destip=desIP.getText();
			//��ȡ�Է��˿ں�
			String destport=desPort.getText();
			
			Date date=new Date();
			String content=clientName.getText()+" "+dateformat.format(date)+"\n";
			content+=sendText.getText();
			// ������Ϣ
			senderThread=new Sender(new Message(content,localip,destip,destport));
			senderThread.start();
			chatArea.setText(chatArea.getText()+"\n"+content+"\n");
			//��������
			sendText.setText(null);
		}else if(e.getActionCommand().equals("ȷ��")){
			this.setTitle(clientName.getText()+"������ͻ���");
			this.localport=Integer.parseInt(localPort.getText());
			
			//���������߳�
			receiverThread=new Receiver(this);
			receiverThread.start();
		}
	}

}
