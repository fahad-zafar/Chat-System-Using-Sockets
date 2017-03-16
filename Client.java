
/*
 * 14-4347	Ali Asgher
 * 14-4027	Usman Nazir
 * 14-4225	Sara Tanzeel
 * 14-4048	Muhammad Fahad Zafar
 */

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.io.*;
import java.net.*;
import java.awt.event.*;

public class Client extends JFrame {

	final static String INET_ADDR = "224.0.0.3";
	final static int PORT = 8888;
	private JPanel contentPane;
	private JTextField username;
	private JTextField msg;
	private JTextArea textArea;
	private static JButton btnUnregister;
	private static JButton btnSendMessage;
	private static JButton btnRegister;
	private static JLabel label;
	private static boolean isDisconnected;
	static String user;
	static Socket sock;
	BufferedReader reader;
	static PrintWriter writer;

	// nested thread class (each client receives messages through this thread)
	public class IncomingReader extends Thread {
		public void run() {
			String[] data;
			String msg;
			try {
				while (true) {

					if (!isDisconnected) {						
						MulticastSocket s = new MulticastSocket(PORT);					// multicast socket
						s.joinGroup(InetAddress.getByName(INET_ADDR));					// join group
						byte buf[] = new byte[1024];									// to store messsage in
						DatagramPacket pack = new DatagramPacket(buf, buf.length);
						s.receive(pack);
						msg = new String(buf, 0, buf.length);
						data = msg.split(":");											// split into array

						// "chat" keyword is concatenated at the end of each message
						if (data[2].contains("Chat"))									
							textArea.append(data[0] + ": " + data[1] + "\n");

						// "disconnect" keyword is concatenated when some client diconnects
						else if (data[2].contains("Disconnect")) 
							textArea.append(data[0] + " has Disconnected.\n");

						s.close();
					}
					else {
						break;
					}
				}
			} catch (Exception e) { 
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Client frame = new Client();
					frame.setVisible(true);
					frame.addWindowListener(new WindowAdapter() {
						
						// extra check to properly close the socket if client closes dialog box without registering
						public void windowClosing(WindowEvent e) {
							try {
								writer.println(user + ": :Disconnect"); 
								writer.flush(); 
								sock.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Client() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblClient = new JLabel("Client");
		lblClient.setFont(new Font("Calibri", Font.BOLD, 16));
		lblClient.setHorizontalAlignment(SwingConstants.CENTER);
		lblClient.setBounds(10, 11, 414, 30);
		contentPane.add(lblClient);

		JLabel lblEnteruser = new JLabel("Enter user:");
		lblEnteruser.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblEnteruser.setBounds(10, 50, 87, 14);
		contentPane.add(lblEnteruser);

		username = new JTextField();
		username.setBounds(107, 47, 200, 20);
		contentPane.add(username);
		username.setColumns(10);

		label = new JLabel("");
		label.setFont(new Font("Calibri", Font.PLAIN, 14));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setBounds(10, 78, 297, 25);
		contentPane.add(label);

		btnRegister = new JButton("Register");
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// setting entered username on top
				user = username.getText();
				username.setEditable(false);
				lblClient.setText("Welcome " + user);

				try 
				{
					sock = new Socket("localhost", PORT);
					InputStreamReader streamreader = new InputStreamReader(sock.getInputStream());
					reader = new BufferedReader(streamreader);
					writer = new PrintWriter(sock.getOutputStream());
					isDisconnected = false;									// must be false if user is online
					writer.println(user + ":has connected.:Connect");
					writer.flush(); 
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				new IncomingReader().start();								// new thread created for receiving messages
				label.setText("You're connected to the Server");
				btnRegister.setEnabled(false);
				btnSendMessage.setEnabled(true);
				btnUnregister.setEnabled(true);
			}
		});
		btnRegister.setBounds(317, 46, 107, 23);
		contentPane.add(btnRegister);

		btnUnregister = new JButton("Unregister");
		btnUnregister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				label.setText ("You've lost connection to the server");
				isDisconnected = true;										// user is disconnected
				try {
					
					// concatenate "disconnect" keyword at disconnection message
					writer.println(user + ": :Disconnect"); 
					writer.flush(); 
					sock.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				btnRegister.setEnabled(true);
				btnSendMessage.setEnabled(false);
				btnUnregister.setEnabled(false);
			}
		});
		btnUnregister.setBounds(317, 80, 107, 23);
		contentPane.add(btnUnregister);

		textArea = new JTextArea();
		textArea.setBounds(10, 114, 414, 73);		
		JScrollPane sp = new JScrollPane(textArea); 
		sp.setBounds(10, 114, 414, 73);
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {  
			public void adjustmentValueChanged(AdjustmentEvent e) {  				// adding a scroll bar
				e.getAdjustable().setValue(e.getAdjustable().getMaximum());  
			}
		});
		textArea.setEditable(false);
		contentPane.add(sp);

		msg = new JTextField();
		msg.setBounds(10, 198, 414, 20);
		contentPane.add(msg);
		msg.setColumns(10);

		btnSendMessage = new JButton("Send Message");
		btnSendMessage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					
					// concatenate "chat" keyword to the end of message
					writer.println(user + ":" + msg.getText() + ":" + "Chat");
					writer.flush();
					msg.setText("");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		btnSendMessage.setBounds(155, 229, 123, 23);
		contentPane.add(btnSendMessage);

		btnSendMessage.setEnabled(false);
		btnUnregister.setEnabled(false);
	}
}
