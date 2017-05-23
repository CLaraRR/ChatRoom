package TCPMultiChatRoom;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class LoginFrame extends JFrame{
	  
	private JButton jbt = new JButton("进入聊天室");
	  private JTextField jtfname= new JTextField(8);
	
	 public static void main(String[] args) {
		    
		  new LoginFrame();
	  }
	 public LoginFrame(){
		 final JPanel p1 = new JPanel();
		 p1.setLayout(new BorderLayout());
		 p1.add(new JLabel("输入用户名"), BorderLayout.WEST);
		 p1.add(jtfname, BorderLayout.CENTER);
		 jtfname.setHorizontalAlignment(JTextField.LEFT);
		 p1.add(jbt, BorderLayout.EAST);
		 p1.setLayout(new FlowLayout(FlowLayout.LEFT,10,20));
		 p1.setSize(300, 100);
		 setLayout(new BorderLayout());
		 add(p1, BorderLayout.CENTER);
		 add(new JLabel("欢迎使用多人聊天系统  ，请输入用户名即可进入聊天室  "), BorderLayout.NORTH);
		 setTitle("多人聊天室 ");
		 setSize(350, 150);
		 setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 setVisible(true);
		 
		 jbt.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					// TODO Auto-generated method stub
					String username = jtfname.getText().trim();
					if(username.equals("")){
					}else{
						//关闭设置页面，启动聊天框页面
					setVisible(false);
					new ChatClient(username);
					}
				}
			}); // Register listener
	 }
}
