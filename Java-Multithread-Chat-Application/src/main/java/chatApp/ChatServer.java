/**
 *
 */
package chatApp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ChatServer {
	Vector<String> users = new Vector<String>();
	Vector<HandleClient> clients = new Vector<HandleClient>();

	public void process() throws Exception {
		ServerSocket server = new ServerSocket(9999, 10);
		System.out.println("Server Started...");
		while (true) {
			Socket client = server.accept();
			HandleClient c = new HandleClient(client);
			clients.add(c);
		} // end of while
	}

	public static void main(String... args) throws Exception {
		new ChatServer().process();
	} // end of main

	public void broadcast(String user, String message) {
		// send message to all connected users
		for (HandleClient c : clients) {
			if (!c.getUserName().equals(user)) {
				c.sendMessage(user, message);
			}
		}
	}

	class HandleClient extends Thread {
		String name = "";
		BufferedReader input;
		PrintWriter output;

		public HandleClient(Socket client) throws Exception {
			// get input and output streams
			input = new BufferedReader(new InputStreamReader(client.getInputStream()));
			output = new PrintWriter(client.getOutputStream(), true);
			// read name
			name = input.readLine();
			if (users.contains(name)) {
				output.println("Sorry, that username is already taken. Please choose another username.");
				client.close();
				clients.remove(this);
				users.remove(name);
				return;
			} else {
				users.add(name); // add to vector
			}
			start();
		}

		public void sendMessage(String uname, String msg) {
			output.println(uname + ":" + msg);
		}

		public String getUserName() {
			return name;
		}

		@Override
		public void run() {
			String line;
			try {
				while (true) {
					line = input.readLine();
					if (line == null || line.equals("quit")) {
						output.println("Goodbye " + name);
						clients.remove(this);
						users.remove(name);
						input.close();
						output.close();
						System.exit(0);
						break;
					} else if (line.equals("")) {
						output.println("Plsease Write something!");
					} else {
						broadcast(name, line); // method of outer class - send messages to all
					}

				} // end of while
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		} // end of run()
	} // end of inner class
} // end of Server
