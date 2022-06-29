package pcd.projecto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;


public class ServerExtra {
	 private ServerSocket serverSocket;
	    private ArrayList<String> nodes;
		
	    public ServerExtra(final int port) throws IOException {
			super();
			this.serverSocket = new ServerSocket(port);;
			this.nodes = new ArrayList<String>();
		}
	    
	    public void iniateServer() throws IOException {
	    	System.err.println("initiating server...");
	    	try {  
	    	while (true) {
	    		  final Socket s = this.serverSocket.accept();
	    		  ManagerClient mc = new ManagerClient(s);
	    		  new Thread(mc).start();
	    	}
	    	  }
	    		catch (IOException e) {
                  System.err.println("Error:Cliet cant connect");
	    	  }  
	    }
	    
	    public static void main(String[] args) {
	    	 if (args.length != 1) {
	             throw new RuntimeException("No port as argument");
	         }
	    	 try {
				new ServerExtra(Integer.parseInt(args[0])).iniateServer();
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }

	    public class ManagerClient implements Runnable{

	    	private Socket socketClient;
	    	private LinkedList<String> temp= new LinkedList<String>();
	    	
			public ManagerClient(final Socket socketClient) {
				super();
				this.socketClient = socketClient;
			}
			
			
			 public LinkedList<String> getTemp() {
				return temp;
			}


			private void clientConsultation(final PrintWriter out) {
		            for (int i = 0; i != ServerExtra.this.nodes.size(); ++i) {
		            	out.println("node " + ServerExtra.this.nodes.get(i));
		            }
		            out.println("END");
		        }
		        
			 	private void addToListTemp(final String address, final int clientPort) {
			 		temp.add(String.valueOf(address) + " " + clientPort);			 	
			 }
		       
			 	private void clientRegistation(final String address, final int clientPort) {
		            ServerExtra.this.nodes.add(String.valueOf(address) + " " + clientPort);
		        }
		        
		        private void clientCancelation(LinkedList<String> temp) {
		        	ServerExtra.this.nodes.remove(temp.getFirst());
		        }
		        @Override
			public void run() {
				// TODO Auto-generated method stub
		        	try {
		                final BufferedReader in = new BufferedReader(new InputStreamReader(this.socketClient.getInputStream()));
		                final PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.socketClient.getOutputStream())), true);
		                final String clientMsg = in.readLine();
		                final String[] subscribeMsg = clientMsg.split(" ");
		                if (subscribeMsg.length < 2 || !subscribeMsg[0].equals("INSC")) {
		                    System.err.println("ERROR client invalid message: " + clientMsg);
		                    return;
		                }
		                final String address = subscribeMsg[1];
		                final int clientPort = Integer.parseInt(subscribeMsg[2]);
		                this.clientRegistation(address, clientPort);
		                this.addToListTemp(address, clientPort);
		                System.err.println("Client Subscribed:" + this.socketClient.getInetAddress().getHostAddress() + " " + clientPort);
		                while (true) {
		                    final String posSubMsg = in.readLine();
		                    System.err.println("Message Received: " + posSubMsg);
		                    if(posSubMsg.equals("nodes")) {
		                    	  this.clientConsultation(out);		             
		                        }
		                    else {
		                    System.err.println("Client Message Invalid: " + posSubMsg);
		                    } 
		                }
		            }
		            catch (IOException e) {
		                System.err.println("ERROR cant connected with client");
		                System.err.println("removing client from node list");
		                System.err.println("Client removed: " + getTemp());
		                clientCancelation(getTemp());
		                
		            }
		        }
	    	
	    }
}
