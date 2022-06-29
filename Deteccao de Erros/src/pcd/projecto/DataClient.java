package pcd.projecto;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;
import java.io.InputStream;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;


import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;


public class DataClient{

	int width = 700;
	int height = 700;
	private static JFrame frame = new JFrame("Client");
	String addressClient;
	int nodePortClient;
	StorageNode sd;
	public LinkedList<StorageNode>nodes = new LinkedList<StorageNode>();
	InputStream is;
	ObjectInputStream ois;
	OutputStream os;
	ObjectOutputStream oos;
	Socket socketCliente;

	

	public DataClient(String args,String args1) throws IOException, ClassNotFoundException	{
		super();
		this.addressClient= args;
		
		this.nodePortClient = Integer.parseInt(args1);
		socketCliente = new Socket(addressClient, nodePortClient);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		addFrameContent();
		frame.pack();
		open();
		
	}

	public void iniateConnectionToServer() throws IOException {

	}

	public String getAddressClient() {
		return addressClient;
	}


	public int getNodePortClient() {
		return nodePortClient;
	}


	public void addNode(StorageNode node) {
		nodes.add(node);
		System.out.println("node adicionado");
	}

	private void addFrameContent() {
		frame.setLayout(new BorderLayout());

		//painel de procura 
		JPanel panel1 = new JPanel();
		frame.add(panel1,BorderLayout.NORTH);

		
		//painel de resposta
		JPanel panel2 = new JPanel();
		panel2.setLayout(new FlowLayout(FlowLayout.LEFT));
		frame.add(panel2);
		
		//scroll 
		frame.add(new JScrollPane(panel2));
		
		//elementos dos paneis
		JLabel indexInfo =  new JLabel("Posição a consultar:");
		JLabel lenghtInfo = new JLabel("comprimento:");
		JTextField index = new JTextField();
		JTextField lenghtData = new JTextField();
		JButton search = new JButton("Consultar");
		JTextArea searchResult = new JTextArea();
		searchResult.setEditable(false);
		index.setColumns(10);
		lenghtData.setColumns(10);

		//panel1 elementos
		panel1.add(indexInfo);
		panel1.add(index);
		panel1.add(lenghtInfo);
		panel1.add(lenghtData);
		panel1.add(search);

		//panel2 elementos
		panel2.add(searchResult);
		searchResult.setRows(width);
		searchResult.setColumns(height);



		try {
			is = socketCliente.getInputStream();
			ois = new ObjectInputStream(is);
			os = socketCliente.getOutputStream();
			oos = new ObjectOutputStream(os);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//butao acçao
		search.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(e.getSource()==search) {
					int startIndex = Integer.parseInt(index.getText());
					int lenght = Integer.parseInt(lenghtData.getText());
					ByteBlockRequest bbr = new ByteBlockRequest(startIndex, lenght);
					try {
						CloudByte[] arrayFromServerNode = null;
						oos.writeObject(bbr);
						System.out.println("Byte sended: " + bbr);
						arrayFromServerNode = (CloudByte[]) ois.readObject();
						//System.out.println("array received: " + Arrays.deepToString(arrayFromServerNode));
						searchResult.setText(Arrays.deepToString(arrayFromServerNode));
					} catch (IOException | ClassNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}				
				}
			}
		});


	}
	public void lerNodeList() {
		ListIterator<StorageNode> iterator = nodes.listIterator();
		while( iterator.hasNext()) {
			System.out.println(iterator.next());
		}
		System.out.println("list empty");
	}


	public void open() {
		frame.setVisible(true);
		frame.setSize(width, height);
		frame.setLocationRelativeTo(null);

	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		new DataClient(args[0], args[1]);
	}


}
