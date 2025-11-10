package myfirstnetworkapp;
import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class calculator_server {
	public static void main(String[] args) throws IOException {
		int nPort = 1234; // The port the server listens on
        
		// Create a fixed thread pool that can run 10 client handlers simultaneously
        ExecutorService threadPool = Executors.newFixedThreadPool(10); 
        
        try (ServerSocket welcomeSocket = new ServerSocket(nPort)) {
            System.out.println("Server start.. (port#=" + nPort + ")");

            // Main server loop: endlessly wait for new connections
            while (true) {
            	// 1. Block and wait for a new client to connect
                // This 'connectionSocket' is the specific socket for this one client
                Socket connectionSocket = welcomeSocket.accept();
                System.out.println("Client connected from " + connectionSocket.getInetAddress());

                 // 2. Create a 'task' (Runnable) to handle this client
                Runnable task = new Calculator(connectionSocket);
                // 3. Give the task to the thread pool.
                // A worker thread will execute the task's run() method.
                // The main server thread immediately loops back to accept()
                // to wait for the next client.
                threadPool.execute(task);
            }
        } catch (IOException e) {
        	// This error often means the port 1234 is already in use
            System.err.println("Server could not listen on port: " + nPort);
            threadPool.shutdown(); // Stop the thread pool if server fails
        }
    }
}
