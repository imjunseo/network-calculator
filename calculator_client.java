package myfirstnetworkapp;
import java.net.*;
import java.io.*;

public class calculator_client {
	public static void main(String[] args) throws IOException {
		// Load server IP and Port from the config file
		ServerConfig config = new ServerConfig("server_info.txt");

        try ( 
        	    // 1. Create a stream to read input from the user's keyboard
        		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        		// 2. Create the client socket and connect to the server
                Socket clientSocket = new Socket(config.getServerIP(), config.getPort());
        		// 3. Create a stream to send data to the server
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        		// 4. Create a stream to read data from the server
        		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
        ) { 
            System.out.println("Enter command (e.g., ADD 10 20): ");
            // Read the command line from the user
            String sentence = inFromUser.readLine(); 

            if (sentence == null) {
                return;
            }
            clientSocket.setSoTimeout(10_000);

            // Send the command to the server
            // '\n' is crucial as the server uses readLine()
            outToServer.writeBytes(sentence + '\n');

            // Read the first line (e.g., "STATUS: 200")
            String statusLine = inFromServer.readLine();
            // Read the second line (e.g., "VALUE: 30.0" or "ERROR: ...")
            String valueLine = inFromServer.readLine();
            
            // Parse the server's response
            if ("STATUS: 200".equals(statusLine) && valueLine != null && valueLine.startsWith("VALUE: ")) {
            	// Success case
                System.out.println("Answer: " + valueLine.substring(7)); 
            } else if (valueLine != null && valueLine.startsWith("ERROR: ")) {
            	// Error case (400, 401, 500)
                System.out.println("Error: " + valueLine.substring(7)); 
            } else {
            	// Should not happen if server protocol is correct
                System.out.println("Error: No complete response from server.");
            }
        } catch (UnknownHostException e) {
            System.err.println("Server not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }
}
