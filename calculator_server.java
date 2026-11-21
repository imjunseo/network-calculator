package myfirstnetworkapp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class calculator_server {
    public static void main(String[] args) throws IOException {
        int port = args.length == 0 ? 1234 : Integer.parseInt(args[0]);
        ThreadPoolExecutor workers = new ThreadPoolExecutor(
            10, 10, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(32)
        );

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Listening on port " + server.getLocalPort());
            while (true) {
                Socket connection = server.accept();
                try {
                    workers.execute(new Calculator(connection));
                } catch (RejectedExecutionException busy) {
                    connection.close();
                }
            }
        } finally {
            workers.shutdown();
        }
    }
}
