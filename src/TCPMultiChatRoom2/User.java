package TCPMultiChatRoom2;

public class User {
	private String name;
	private String ipAddr;
	
	public User(String userDescription) {
		String items[] = userDescription.split("%");
		this.name = items[0];
		this.ipAddr = items[1];
	}

	public User(String name, String ipAddr) {
		this.name = name;
		this.ipAddr = ipAddr;
	}

	public String getName() {
		return name;
	}

	public String getIpAddr() {
		return ipAddr;
	}

	public String description() {
		return name + "%" + ipAddr;
	}
}

