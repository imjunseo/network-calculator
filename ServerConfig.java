package myfirstnetworkapp;
import java.io.*;

public class ServerConfig {
	private String serverIP;
    private int port;
    
     /*
     * Constructor for ServerConfig.
     * Attempts to read server IP and port from the specified configuration file.
     * If the file is not found or is malformed, it defaults to localhost:1234.
     */
    public ServerConfig(String configFileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(configFileName))) {
        	// Read the first line as IP address
        	this.serverIP = br.readLine();
        	// Read the second line as Port
            this.port = Integer.parseInt(br.readLine());
            if (serverIP == null || serverIP.isBlank() || port < 1 || port > 65_535) {
                throw new NumberFormatException("Invalid host or port");
            }
            this.serverIP = serverIP.strip();
            System.out.println("Config file loaded: " + serverIP + ":" + port);
        } catch (FileNotFoundException e) {
            System.err.println("Config file not found. Using default settings.");
            this.serverIP = "localhost";
            this.port = 1234;
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading config file. Using default settings.");
            this.serverIP = "localhost";
            this.port = 1234;
        }
    }
    public String getServerIP() {
        return serverIP;
    }

    public int getPort() {
        return port;
    }
}
