package pcd.projecto;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;


public class StorageNode{


	//canais de conecção com o diretorio
	InputStreamReader inputStreamReader;
	OutputStreamWriter outputStreamWriter;
	private BufferedReader bufferedReader ;
	BufferedWriter bufferedWriter;
	private PrintWriter printWriter ;
	BufferedOutputStream bufferedOutputStream;
	//canais de objectos
	InputStream inputStream ;
	ObjectInputStream objectInputStream ;
	OutputStream outputStream;
	ObjectOutputStream objectOutputStream;

	static ArrayList<CloudByte> byteData = new ArrayList<CloudByte>();
	LinkedList<NodeInfo> nodes = new LinkedList<NodeInfo>();
	LinkedList<CloudByte> errorsDetected = new LinkedList<CloudByte>();
	LinkedList<CloudByte> errorsCorrected = new LinkedList<CloudByte>();
	//atributos do StorageNode
	int portDir;
	String address;
	int portNode;
	File data;
	protected ServerSocket serverSocket;

	public StorageNode(String args,String args1 , String args2, String args3) throws IOException, ClassNotFoundException, InterruptedException {
		super();
		this.address = args;
		this.portDir = Integer.parseInt(args1);
		this.portNode = Integer.parseInt(args2);
		this.data = new File(args3);
		cloudByteArray2(data);
		startNodeProcedure();
		runClient();
	}



	public StorageNode(String args,String args1 , String args2) throws IOException, ClassNotFoundException, InterruptedException {
		super();
		this.address = args;
		this.portDir = Integer.parseInt(args1);
		this.portNode = Integer.parseInt(args2);
		startNodeProcedure();
		readNodesAvailable();
		iniateDownloadData();
		while(itsIncomplete(getByteData())) {
			Thread.sleep(0);
		}
		runClient();
	}


	@SuppressWarnings("unused")
	private void iniateDownloadData() throws IOException {
		DealWithClient dealwithClient = new DealWithClient(getNodes(), getPortNode(), getAddress(),getByteData());
		removeElementsList(getNodes());
	}

	private void setupServerNode() {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					serverSocket = new ServerSocket(getPortNode());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				while(true) {
					System.out.println("espera por novas ligacoes....");
					Socket socketClient;
					try {
						socketClient = serverSocket.accept();
						System.out.println("Novo cliente ligou-se!");
						outputStream = socketClient.getOutputStream();
						objectOutputStream = new ObjectOutputStream(outputStream);
						inputStream = socketClient.getInputStream();
						objectInputStream = new ObjectInputStream(inputStream);
						System.out.println("channels ready");
						ClientHandler clientSock = new ClientHandler(socketClient, objectOutputStream, objectInputStream);
						new Thread(clientSock).start();
						System.err.println("iniating download");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}

				}

			}

		});
		t.start();

	}

	private class ClientHandler implements Runnable{
		private Socket s;
		private ObjectOutputStream oos;
		private ObjectInputStream ois;
		private volatile boolean running = true;
		public ClientHandler(Socket s, ObjectOutputStream oos, ObjectInputStream ois) {
			super();
			this.s = s;
			this.oos=oos;
			this.ois = ois;
		}

		public void stop() {
			running = false;
		}

		@Override
		public void run() {
			ByteBlockRequest bbr;
			while (running) {
				try {
					bbr = (ByteBlockRequest) ois.readObject();
					sendArrayCloudByte(bbr, oos);
					oos.flush();
				} catch (ClassNotFoundException | IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					System.err.println("end of download");
					try {

						this.ois.close();
						this.oos.close();
						this.s.close();
						stop();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}

			}

		}
	}

	private synchronized boolean errorDetectorByteRequest(CloudByte cb) {
		if(cb.isParityOk()) {
			return false;
		}
		System.out.println("Error detected!");
		//se encontra chama o processo de corecção

		return true;
	}

	public synchronized void howManyNodesAvailable(ByteBlockRequest bbr, CountDownLatch latch) throws UnknownHostException, IOException {
		readNodesAvailable();
		for(NodeInfo nI : nodes) {
			if(nI.getNodePort()!= getPortNode()) {
				ErrorCorrector ec = new ErrorCorrector(nI.getNodePort(),nI.getAddress(),bbr, latch);
				System.out.println("created Error Corrector client " + nI.getNodePort() + " and " + nI.getAddress());
				new Thread(ec).start();
			}
		}
	}

	public CloudByte correctError(LinkedList<CloudByte> list) {
		LinkedList<CloudByte> errorCorrectedTemp = new LinkedList<CloudByte>();
		errorCorrectedTemp.add(list.get(0));
		errorCorrectedTemp.add(list.get(1));
		System.out.println("First answer " + errorCorrectedTemp.get(0));
		System.out.println("Second answer " + errorCorrectedTemp.get(1));
		if(errorCorrectedTemp.stream().distinct().count()==1) {
			System.out.println("all answers are the same");
			return errorCorrectedTemp.getFirst();
		}
		System.out.println("Answers arent equal returning the same cloudbyte");
		return list.getFirst();
		
	}

	
	public void resetList(LinkedList<CloudByte> list) {
		while(!list.isEmpty()) {
			list.removeFirst();
		}
	}
	private void removeElementsList(LinkedList<NodeInfo> list) {
		while(!list.isEmpty()) {
			list.removeFirst();
		}
	}
	private class ErrorDetectorThread implements Runnable{
		ArrayList<CloudByte> array;
		@SuppressWarnings("unused")
		LinkedList<CloudByte> errorList;


		public ErrorDetectorThread(ArrayList<CloudByte> array,LinkedList<CloudByte> errorList) {
			super();
			this.array = array;
			this.errorList= errorList;
			System.out.println("detection start");
		}


		public ArrayList<CloudByte> getArray() {
			return array;
		}


		@Override
		public void run() {
			try {
				searchError(getArray(),getErrorsDetected());
			} catch (InterruptedException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		}


		public synchronized void searchError(ArrayList<CloudByte> array, LinkedList<CloudByte> errorList) throws InterruptedException, UnknownHostException, IOException{
			while (true) {	
				Thread.sleep(15000);
				for (CloudByte cb : array) {			
					if(errorDetectorByteRequest(cb)){	
							while(errorList.contains(cb)) {
								System.out.println("error already detected" + " Thread " + Thread.currentThread() + "continue search...");
								wait();
							}
							errorList.add(cb);
							CountDownLatch latch = new CountDownLatch(2);
							ByteBlockRequest bbr = new ByteBlockRequest(array.indexOf(cb),1);
							System.out.println("error detected and ready to correct");
							howManyNodesAvailable(bbr, latch);
							latch.await();
							CloudByte temp = correctError(errorsCorrected);
							array.set(array.indexOf(cb), temp);
							System.out.println("error: " + cb + " corrected to " + temp + " Thread " + Thread.currentThread());		 					
							resetList(errorsCorrected);
							removeElementsList(getNodes());
							errorList.remove(cb);
							notifyAll();
					}
						
					}
				}
			}
		

	}
	private class ErrorCorrector implements Runnable{
		Socket socketClient;
		OutputStream os;
		ObjectOutputStream oos;
		InputStream is;
		ObjectInputStream ois;
		int portNode;
		String address;
		ByteBlockRequest bbr;
		CountDownLatch latch;
		public ErrorCorrector(int portNode, String address, ByteBlockRequest bbr ,CountDownLatch latch) throws UnknownHostException, IOException {
			super();
			this.portNode = portNode;
			this.address = address;
			this.bbr = bbr;
			this.latch = latch;
			socketClient = new Socket(getAddress(),getPortNode());
			is = socketClient.getInputStream();
			ois = new ObjectInputStream(is);
			os = socketClient.getOutputStream();
			oos = new ObjectOutputStream(os);
		}


		public ByteBlockRequest getBbr() {
			return bbr;
		}


		public int getPortNode() {
			return portNode;
		}

		public String getAddress() {
			return address;
		}

		@Override
		public void run() {
			try {
				errorCorrection();
				latch.countDown();
				System.out.println("Thread " + Thread.currentThread() + "finished");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		public void errorCorrection() throws IOException, ClassNotFoundException {
			oos.writeObject(getBbr());	
			System.out.println("sended " + getBbr());
			CloudByte[] arrayFromServerNode = null;
			arrayFromServerNode = (CloudByte[]) ois.readObject();
			System.out.println("received " + Arrays.deepToString(arrayFromServerNode));
			errorsCorrected.add(arrayFromServerNode[0]);
		}
	}


	public void threadCorrectionRunning() throws InterruptedException {
		System.out.println("ALL data ready");
		ErrorDetectorThread ed1 = new ErrorDetectorThread(getByteData(),getErrorsDetected());
		ErrorDetectorThread ed2 = new ErrorDetectorThread(getByteData(),getErrorsDetected());
		new Thread(ed1).start();
		new Thread(ed2).start();

	}

	public void runClient() throws IOException, ClassNotFoundException, InterruptedException {
		while (true) {
			System.out.println("IM a server now");
			setupServerNode();
			threadCorrectionRunning();
			readInputConsole();


		}
	}

	private void sendArrayCloudByte(ByteBlockRequest bbr , ObjectOutputStream oos) throws UnknownHostException, IOException, InterruptedException {
		int startIndex = bbr.getStartIndex();
		int lenght = bbr.getLenght();
		int limit = startIndex + lenght;
		int i = 0;
		CloudByte[] arrayToSend = new CloudByte[lenght];
		while(startIndex<limit && i< lenght) {
			if(errorDetectorByteRequest(getByteData().get(startIndex))) {
				System.out.println("we need to correct " + getByteData().get(startIndex));
				ByteBlockRequest bbrToCorrect = new ByteBlockRequest(startIndex, 1);
				CloudByte temp = correctCloudByte(bbrToCorrect);
				System.out.println("error corrected to " + temp);
				arrayToSend[i]=temp;
				getByteData().set(getByteData().indexOf(getByteData().get(startIndex)), temp);
				resetList(errorsCorrected);
				removeElementsList(getNodes());	
			}else {
				arrayToSend[i]=getByteData().get(startIndex);
			}
			i++;
			startIndex++;	
		}

		try {
			oos.writeObject(arrayToSend);
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}


	private CloudByte correctCloudByte(ByteBlockRequest bbr) throws UnknownHostException, IOException, InterruptedException {
		CountDownLatch latch = new CountDownLatch(2);
		System.out.println("error detected and ready to correct");
		howManyNodesAvailable(bbr, latch);
		removeElementsList(getNodes());
		latch.await();							
		return correctError(errorsCorrected);
	}



	private void startNodeProcedure() throws IOException  {//conectar dir / inscrever
		System.out.println("Node running...");
		connectToServer();
		sendMessage();
		System.out.println("Server: inscrição com sucesso");
	}


	private void readNodesAvailable() throws IOException {//ler nodes isncritos no dir
		String sendToDirectory = "nodes";
		System.out.println("sending to directory: " + sendToDirectory);
		printWriter.println(sendToDirectory);
		String infoDirectory = null;
		while( (infoDirectory = bufferedReader.readLine()) != null) {
			System.out.println("DIRECTORY: " + infoDirectory);
			processInfoNode(infoDirectory);
			if(infoDirectory.equals("END")) {
				break;
			}
		}

	}

	private void processInfoNode(String infoDirectory) {//guardar na lista os nodes do diretorio
		if(!infoDirectory.equals("END")) {
			String [] infoNode = infoDirectory.split(" ",3);
			String addressNode = infoNode[1];
			String portNode = infoNode[2];
			NodeInfo nI = new NodeInfo(addressNode,Integer.valueOf(portNode));
			if(!nodes.contains(nI)) {
				nodes.add(nI);
				System.out.println("node added to list");
			}
			interateList();
		}
	}


	public void interateList() {//lista de nodes inscritos
		Iterator<NodeInfo> it = nodes.iterator();
		while(it.hasNext()) {
			System.out.println(it.next() + " ");

		}
	}


	public int getPortNode() {
		return portNode;
	}

	@Override
	public String toString() {
		return "StorageNode [portDir=" + portDir + ", address=" + address + ", portNode=" + portNode + ", data=" + data
				+ "]";
	}

	private void readInputConsole() throws IOException {
		while (true) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String s = br.readLine();
			if (s.contains(" ")) {
				String [] commands = s.split(" ",2);
				String instructionPart1 = commands[0];
				String instructionPart2 = commands[1];
				System.out.println("comando: " + instructionPart1+ " " + instructionPart2);
				if (instructionPart1.equals("ERROR")) {
					System.out.println("BYTE CORRUPTION INJECTED");
					int index = Integer.parseInt(instructionPart2);
					if(getByteData().get(index).isParityOk()) {
						System.out.println(getByteData().get(index) + "--> BEFORE CORRUPTION");
						getByteData().get(index).makeByteCorrupt();
						System.out.println(getByteData().get(index) + "--> AFTER CORRUPTION");
					}
					else{
						System.out.println("BYTE ALLREADY CORRUPTED");
					}
				}
			}
			if(s.equals("nodes")){
				System.out.println("NODES ON DIRECTORY...");
				//envio o nodes
				printWriter.println(s);
				// recebo a informação do diretorio
				String infoDirectory;
				while( (infoDirectory = bufferedReader.readLine()) != null) {
					System.out.println("DIRECTORY: " + infoDirectory);
					if(infoDirectory.equals("END")) {
						break;
					}
				}
			}
		}
	}


	private void sendMessage() throws IOException {
		Subscription sub = new Subscription(address, portNode);
		System.out.println("sending to directory: " + sub.messageSub());
		outputStreamWriter.write(sub.messageSub() + "\n");
		outputStreamWriter.flush();
	}

	private void connectToServer() throws IOException {
		InetAddress address = InetAddress.getByName( null );
		System.out.println(" Endereco : " + address);
		@SuppressWarnings("resource")
		Socket socketClient = new Socket ( address ,8080 );
		System.out.println(" Socket : " + socketClient);
		bufferedReader = new BufferedReader (new InputStreamReader(
				socketClient . getInputStream ()));
		printWriter = new PrintWriter (new BufferedWriter (
				new OutputStreamWriter ( socketClient . getOutputStream ())) ,
				true );	
		bufferedOutputStream = new BufferedOutputStream(socketClient.getOutputStream());
		outputStreamWriter = new OutputStreamWriter(bufferedOutputStream);
	}


	public static void cloudByteArray2(File data) throws IOException{
		@SuppressWarnings("resource")
		DataInputStream data_in = new DataInputStream(new BufferedInputStream(new FileInputStream((data))));
		@SuppressWarnings("unused")
		int index = 0; 
		while(true) {
			try {
				byte value = data_in.readByte();
				CloudByte cb = new CloudByte(value);
				if(cb.isParityOk()) {
					getByteData().add(cb);
				}
				index++;
			}
			catch(EOFException eof) {
				System.err.println ("loaded data from file: "+ getByteData().size());
				break;
			}
		}
	}


	public void corruptingByte(LinkedList<CloudByte> arrayByte,int indexArray) {
		CloudByte byteToCorrupt = arrayByte.get(indexArray) ;
		byteToCorrupt.makeByteCorrupt();
	}

	public String getAddress() {
		return address;
	}

	public int getPortDir() {
		return portDir;
	}

	public File getData() {
		return data;
	}


	public LinkedList<NodeInfo> getNodes() {
		return nodes;
	}



	public static ArrayList<CloudByte> getByteData() {
		return byteData;
	}

	public boolean itsIncomplete(ArrayList<CloudByte> byteData) {
		if(byteData.size()!=1000000) {
			return true;
		}
		return false;
	}

	public LinkedList<CloudByte> getErrorsDetected() {
		return errorsDetected;
	}



	public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException {
		if(args.length == 3 ) {
			new StorageNode(args[0], args[1], args[2]);
		}else {
			new StorageNode(args[0], args[1], args[2],args[3]);
		}
	}
}
