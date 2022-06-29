package pcd.projecto;



public class Subscription {
	int port;
	String address;
	public Subscription(String address, int port) {
		super();
		this.address = address;
		this.port = port;
	}


	public int getPort() {
		return port;
	}


	public void setPort(int port) {
		this.port = port;
	}


	public String getAddress() {
		return address;
	}


	public void setAddress(String address) {
		this.address = address;
	}


	public String messageSub() {
		String msgSub = "";
		msgSub ="INSC "+ String.valueOf(getAddress())+" "+String.valueOf(getPort());
		//System.out.println(msgSub);
		return msgSub;

	}
}
