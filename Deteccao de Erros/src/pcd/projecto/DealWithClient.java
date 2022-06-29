package pcd.projecto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;



public class DealWithClient {



	BufferedReader bufferedReader;
	BufferedWriter bufferedWriter;
	InputStreamReader inputStreamReader;
	OutputStreamWriter outputStreamWriter;
	LinkedList<ByteBlockRequest> requests = new LinkedList<ByteBlockRequest>();
	ArrayList<ByteBlockRequest> requestsTest = new ArrayList<ByteBlockRequest>();
	LinkedList<CloudByte> byteProxyArray = new LinkedList<CloudByte>();
	LinkedList<NodeInfo> nodeInfo = new LinkedList<NodeInfo>();
	int portNode;
	String address;
	ArrayList<CloudByte> arrayList;
	public DealWithClient(LinkedList<NodeInfo> nodeInfo, int portNode, String address, ArrayList<CloudByte> arrayList) throws IOException {
		super();
		this.nodeInfo = nodeInfo;
		this.portNode = portNode;
		this.address = address;
		this.arrayList = arrayList;
		System.out.println("Download data iniate");
		createProductor();
		createConsumers();
	}


	public LinkedList<NodeInfo> getNodeInfo() {
		return nodeInfo;
	}

	public ArrayList<CloudByte> getArrayList() {
		return arrayList;
	}

	public int getPortNode() {
		return portNode;
	}

	public String getAddress() {
		return address;
	}

	public void removeElementsList() {
		while (!getNodeInfo().isEmpty()) {
			getNodeInfo().removeFirst();
		}
	}

	public void createConsumers() throws IOException {
		for(NodeInfo nI : nodeInfo) {
			if(nI.getNodePort()!= getPortNode()) {
				RequestConsumere rc = new RequestConsumere(this,nI.getNodePort(),nI.getAddress());
				rc.start();
			}
		}
	}

	public void createProductor() {
		RequestProduction rp = new RequestProduction(this);
		rp.start();

	}

	public synchronized void produce(ByteBlockRequest bbr) throws InterruptedException {
		requestsTest.add(bbr);
		notifyAll();
	}

	public synchronized ByteBlockRequest consume() throws InterruptedException {
		while(requestsTest.isEmpty()) {
			wait();
		}
		notifyAll();
		ByteBlockRequest bbr = requestsTest.get(0);
		requestsTest.remove(0);
		return bbr;
	}

	public void iterate(LinkedList<CloudByte> list) {
		for (int i = 0; i < list.size(); i++) {
			System.out.println(list.get(i));
		}
	}

	private class RequestProduction extends Thread{
		private DealWithClient dealwithClient;
		private AtomicBoolean running = new AtomicBoolean(false);

		public RequestProduction(DealWithClient dealwithClient) {
			super();
			this.dealwithClient = dealwithClient;
		}

	
		@Override
		public void run() {
			running.set(true);	
			for(int i = 0; i != 10000; i++) {
	            ByteBlockRequest bbr = new ByteBlockRequest(i*100, 100);
	            try {
					dealwithClient.produce(bbr);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }

		}

	}
	private class RequestConsumere extends Thread{
		private DealWithClient dealwithClient;
		private AtomicBoolean running = new AtomicBoolean(false);
		Socket socketClient;
		OutputStream os;
		ObjectOutputStream oos;
		InputStream is;
		ObjectInputStream ois;
		int portNode;
		String address;
		int numberOfBlock;
		public RequestConsumere(DealWithClient dealwithClient, int portNode, String address) throws IOException {
			super();
			this.dealwithClient = dealwithClient;
			this.portNode = portNode;
			this.address = address; 
			socketClient = new Socket(getAddress(), getPortNode());
			is = socketClient.getInputStream();
			ois = new ObjectInputStream(is);
			os = socketClient.getOutputStream();
			oos = new ObjectOutputStream(os);
		}


		public ObjectOutputStream getOos() {
			return oos;
		}


		public ObjectInputStream getOis() {
			return ois;
		}


		public int getPortNode() {
			return portNode;
		}

		public String getAddress() {
			return address;
		}

		@Override
		public String toString() {
			return "RequestConsumere [portNode=" + portNode + "]";
		}

		
		@Override
		public void run() {//teste a funcionar mas sem enviar nada aos nos com file
			try {
				testConnection();

			} catch (ClassNotFoundException | InterruptedException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}


		public void testConnection() throws InterruptedException, IOException, ClassNotFoundException {
			long startTime = System.nanoTime();
			running.set(true);
			ByteBlockRequest bbr;
			while (running.get()) {
				synchronized (this) {	
					bbr = dealwithClient.consume();
					writeToServer(bbr, getOos());
					numberOfBlock ++;
					oos.flush();					
					canIAdd(readFromServer(getOis()), bbr,	getArrayList());
					if(requestsTest.size()==0) {
						System.err.println("Thread" + this + "numberBlocks: " + numberOfBlock);
						long elapsedTime = (System.nanoTime() - startTime);
						double elapsedTimeInSecond = (double) elapsedTime / 1_000_000_000;
						System.err.println("download time " + elapsedTimeInSecond + "seconds");
						if(getArrayList().size()== 1000000) {
							this.oos.close();
							this.socketClient.close();
							this.ois.close();
							running.set(false);
						}
						
					}
				}	
			}
			
		}
	}

	public void writeToServer(ByteBlockRequest bbr, ObjectOutputStream oos) throws IOException {
		oos.writeObject(bbr);

	}

	public CloudByte[] readFromServer(ObjectInputStream ois) throws ClassNotFoundException, IOException {

		CloudByte[] arrayFromServerNode = null;
		arrayFromServerNode = (CloudByte[]) ois.readObject();
		return arrayFromServerNode;

	}

	
	public synchronized void canIAdd(CloudByte[] arrayFromServerNode,ByteBlockRequest bbr, ArrayList<CloudByte> array) throws InterruptedException {
		while(bbr.getStartIndex() != array.size()) {
			wait();
		}
		fillListWithByteRequest(arrayFromServerNode, bbr, array);
		notifyAll();
	}
	

	public void fillListWithByteRequest(CloudByte[] arrayFromServerNode, ByteBlockRequest bbr, ArrayList<CloudByte> array) {
		int i =0;
		while(i<bbr.getLenght()) {
			array.add(arrayFromServerNode[i]);
			i++;
		}	
	}
	
	public LinkedList<CloudByte> orderByteRequests(CloudByte[] arrayFromServerNode, ByteBlockRequest bbr, LinkedList<CloudByte> listToSend) {
		int i =0;
		@SuppressWarnings("unused")
		int startIndex = bbr.getStartIndex();
		if(listToSend.size() == bbr.getStartIndex()) {
			while(i<bbr.getLenght()) {
				listToSend.add(arrayFromServerNode[i]);
				startIndex++;
				i++;
			}
		}
		return listToSend;
	}
}
