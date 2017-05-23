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
	
	private Socket socketConn;// ����Socket����
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
		
		desIPLabel = new JLabel("������IP��");
		desIPLabel.setBounds(20, 10, 64, 15);
		contentPane.add(desIPLabel);
		
		desIP = new JTextField();
		desIP.setBounds(91, 7, 116, 21);
		contentPane.add(desIP);
		desIP.setColumns(10);
		
		desPortLabel = new JLabel("�˿ںţ�");
		desPortLabel.setBounds(237, 10, 67, 15);
		contentPane.add(desPortLabel);
		
		desPort = new JTextField();
		desPort.setBounds(292, 7, 100, 21);
		contentPane.add(desPort);
		desPort.setColumns(10);
		
		clientNameLabel = new JLabel("�ҵ����֣�");
		clientNameLabel.setBounds(20, 35, 69, 15);
		contentPane.add(clientNameLabel);
		
		clientName = new JTextField();
		clientName.setBounds(91, 34, 116, 21);
		contentPane.add(clientName);
		clientName.setColumns(10);
		
		bindButton = new JButton("���ӷ�����");
		bindButton.addActionListener(this);
		bindButton.setBounds(398, 31, 116, 23);
		contentPane.add(bindButton);
		
		localPortLabel = new JLabel("�����˿ںţ�");
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
					socketConn.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}// �ر���Դ
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
			
			
			Date date=new Date();
			String content=clientName.getText()+" "+dateformat.format(date)+"\n";
			content+=sendText.getText();
			System.out.print(content+"\n");
			out.println(content+"\n");// ���͵������
			//chatArea.setText(chatArea.getText()+"\n"+content+"\n");
			//��������
			sendText.setText(null);
		}else if(e.getActionCommand().equals("���ӷ�����")){
			this.setTitle(clientName.getText()+"������ͻ���");
			//��ȡ�Է�IP
			destip=desIP.getText();
			//��ȡ�Է��˿ں�
			destport=Integer.parseInt(desPort.getText());
			//��ȡ�Լ��˿ں�
			this.localport=Integer.parseInt(localPort.getText());
			connectSocket();
			receiveThread=new Receiver();
			receiveThread.start();
			
			
		}
	}



	private void connectSocket() {
		// TODO Auto-generated method stub
		try {
            
            if (destip.equals("localhost") || destip.equals("127.0.0.1")) {// �ж�IP��ַ(����)����Ǳ���localhost
                socketConn = new Socket(InetAddress.getLocalHost(), destport);// ������������
            } else {
                socketConn = new Socket(InetAddress.getByName(destip), destport);// ����Զ������
            }
            //BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));// ��ôӼ����������
            out = new PrintWriter(socketConn.getOutputStream(), true);// ��÷�����д���ݵ�������
            in = new BufferedReader(new InputStreamReader(socketConn.getInputStream()));// ��ý��շ������������ݵĻ�����
            
            
        } catch (SecurityException e) {// ����ȫ�Դ���ʱ�������쳣
            System.out.println("���ӷ��������ְ�ȫ���⣡");
        } catch (IOException e) {// ����IO���쳣
            System.out.println("���ӷ���������I/O����");
        }
	}
	private class Receiver extends Thread{
		public void run(){
			while(true){
				try {
					String info = in.readLine();//�ӷ������˶�ȡ����
					if(info.length()>0){
						chatArea.setText(chatArea.getText()+info+"\n");
						System.out.println("�յ��������Ϣ��"+info);
					}
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}// �ӷ�������ȡ�ַ���
				
			}
		}
	}
	
	public static void main(String[] args){
		new ChatClient();
	}


}
