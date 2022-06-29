package pcd.projecto;


import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import pt.iscte.pcd.directory.Directory;



public class Server {
	int portDirectory;
	public static final int ServerPort = 4999;
	File data = new File("data.bin");
	static ServerSocket serverSocket;
	static Socket socketCliente;
	static ArrayList<StorageNode> clientDealer = new ArrayList<StorageNode>();

	public Server(int portDirectory) throws IOException {
		super();
		this.portDirectory = portDirectory;
	}

	public void setPortDirectory(int portDirectory) {
		this.portDirectory = portDirectory;
	}

	public int getPortDirectory() {
		return portDirectory;
	}

	public void startDirectory() throws IOException {
		Directory directory =  new Directory(portDirectory);
		System.out.println("a iniciar diretorio");
		directory.serve();
	}

	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		new Server(8080).startDirectory();
	}
}

