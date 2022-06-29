package pcd.projecto;



public class NodeInfo {
		String address;
		int nodePort;
	
	public NodeInfo(String address, int nodePort) {
			super();
			this.address = address;
			this.nodePort = nodePort;
		}


	public String getAddress() {
		return address;
	}


	public int getNodePort() {
		return nodePort;
	}


	@Override
	public String toString() {
		return "NodeInfo [address=" + address + ", nodePort=" + nodePort + "]";
	}
	
		
	
}
