package myfirstnetworkapp;

import java.io.*;
import java.net.*;
import java.util.Locale;
import java.util.StringTokenizer;

/*Each instance of this class handles one client connection in a separate thread
(managed by the ThreadPool). */
public class Calculator implements Runnable {
    
    private static final int MAX_REQUEST_LENGTH = 256;
    private final Socket socket; // The specific socket for this one client

    // Constructor: The server passes the client's socket to this task
    public Calculator(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String clientSentence;
        String response; // The 2-line protocol response to send back

        try (
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream())
        ) {
            // 1. Read the command from the client (e.g., "ADD 10 20")
            socket.setSoTimeout(5_000);
            clientSentence = readRequest(inFromClient);
            
            // Log to server console
            if (clientSentence != null) {
                System.out.println("Received a calculation request.");
            } else {
                return; // Client disconnected without sending data
            }

            // 2. Try to parse and compute the result
            try {
                // Use StringTokenizer to parse the command (e.g., "ADD", "10", "20")
                StringTokenizer st = new StringTokenizer(clientSentence);
                if (st.countTokens() != 3) {
                    throw new IllegalArgumentException("expected an operation and two integers");
                }
                String op = st.nextToken().toUpperCase(Locale.ROOT); // "ADD"
                
                long num1 = Integer.parseInt(st.nextToken()); //"10"
                long num2 = Integer.parseInt(st.nextToken()); //"20"
                double result = 0;

                // Perform the four supported operations.
                switch (op) {
                    case "ADD":
                        result = num1 + num2;
                        break;
                    case "SUB":
                        result = num1 - num2;
                        break;
                    case "MUL":
                        result = num1 * num2;
                        break;
                    case "DIV":
                        //Check for division by zero
                        if (num2 == 0) {
                            throw new ArithmeticException("divided by zero");
                        }
                        result = (double) num1 / num2;
                        break;
                    default:
                        // Handle unknown operations like "MIN"
                        throw new IllegalArgumentException("Invalid operation");
                }
                
                //Format success response
                response = "STATUS: 200\nVALUE: " + result + "\n";

            } catch (IllegalArgumentException | ArithmeticException e) {
                // Catch all calculation/parsing errors (e.g., "ADD 10", ArithmeticEx, IllegalArgEx)
                
                //Format error response
                String errorType = e.getMessage();
                if (e instanceof ArithmeticException) {
                    // "divided by zero"
                    response = "STATUS: 400\nERROR: " + errorType + "\n";
                } else {
                    response = "STATUS: 401\nERROR: Invalid command or operands\n";
                }
            }
            
            // 3. Send the 2-line response (success or error) back to the client
            outToClient.writeBytes(response);

        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String readRequest(BufferedReader input) throws IOException {
        StringBuilder request = new StringBuilder();
        int character;
        while ((character = input.read()) != -1 && character != '\n') {
            if (request.length() == MAX_REQUEST_LENGTH) {
                throw new IOException("Request exceeds maximum length");
            }
            request.append((char) character);
        }
        return character == -1 && request.length() == 0 ? null : request.toString().strip();
    }
}
