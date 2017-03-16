
/*
 * 14-4347	Ali Asgher
 * 14-4027	Usman Nazir
 * 14-4225	Sara Tanzeel
 * 14-4048	Muhammad Fahad Zafar
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server {
	
	final static int BROWSER_PORT = 9090;
	final static int PORT = 8888;
	final static String INET_ADDR = "224.0.0.3";
	static ServerSocket server = null;
	static ArrayList <String> history = new ArrayList <String> ();					// saves last 10 messages

	// nested thread class to send messages to all connected clients
	public static class ClientHandler extends Thread {
		BufferedReader reader;
		Socket sock;

		public ClientHandler(Socket clientSocket) {
			try {
				sock = clientSocket;
				InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
				reader = new BufferedReader(isReader);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void run() {
			String msg;
			String[] data;
			try {
				while ((msg = reader.readLine()) != null) {
					data = msg.split(":");

					// if a client connects, add "chat" keyword and send to all
					if (data[2].equals("Connect")) {
						sendToAll((data[0] + ":" + data[1] + ":Chat"));
						if (history.size() == 10) {						// to make sure only last 10 messages are saved
							history.remove(0);
						}
						history.add(data[0] + " has connected");
						new AdminClient().start();						// start an admin thread with each new msg
					}

					// if a client disconnects, add "chat" keyword and send to all
					else if (data[2].equals("Disconnect")) {
						sendToAll((data[0] + ":has disconnected." + ":Chat"));
						if (history.size() == 10) {
							history.remove(0);
						}
						history.add(data[0] + " has disconnected");
						new AdminClient().start();
					}

					// if its a message, 'chat' keyword is already concatenated
					else if (data[2].equals("Chat")) {
						sendToAll(msg);
						if (history.size() == 10) {
							history.remove(0);
						}
						history.add(data[0] + ": " + data[1]);
						new AdminClient().start();
					}
				} 
			} catch (Exception ex) {
				ex.printStackTrace();
			} 
		} 
	}

	// nested thread class (to show last 10 messages in browser
	public static class AdminClient extends Thread {

		private final Object lock = new Object();
		public void run () {
			
			// at a time only 1 thread is executed
			synchronized (lock) {
				Socket socket = null;
				BufferedWriter bufferedWriter = null;
				try {
					socket = server.accept();					// waiting for connection
					bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
					
					// prints messages
					for (String msg : history) {
						bufferedWriter.write((msg + "\n"));
					}
					bufferedWriter.flush();
					
				} catch (IOException e1) {
					e1.printStackTrace();
				} finally {
					try {
						socket.close();
						bufferedWriter.close();
					} catch (Exception e) {
						e.printStackTrace();
					}				
				}
			}
		}
	}

	public static void main (String args[]) throws IOException {
		System.out.println("Server Started");
		new StartServer().start();							// thread for establishing connection
		server = new ServerSocket(BROWSER_PORT);					
		new AdminClient().start();							// admin thread
	}

	// establishes connection with client and server and start a 'ClientHandler' thread for each client
	public static class StartServer extends Thread {
		public void run() { 
			ServerSocket serverSock = null;
			try {
				serverSock = new ServerSocket(PORT);
				while (true) {
					Socket clientSock = serverSock.accept();
					new ClientHandler(clientSock).start();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// using multicast to send received message to all connected users
	public static void sendToAll(String message) throws IOException {		
		byte buf [] = message.getBytes();
		MulticastSocket s = new MulticastSocket();
		DatagramPacket pack = new DatagramPacket(buf, buf.length,
				InetAddress.getByName(INET_ADDR), PORT);
		s.send(pack);
		s.close();
	}
}
