package Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread extends Thread {
    private Socket socket;
    private ArrayList<ServerThread> threadList;
    private PrintWriter output;
    
    /* This is the constructor of ServerThread.
    Each Thread runs independently in the code.
    In this case, the thread is used to read clients
    and send messages to clients.
    The ServerThread needs to receive:
    1. Socket: information about the client's socket
    2. ArrayList: the list of connected clients
    */
    public ServerThread(Socket socket, ArrayList<ServerThread> threads) {
        this.socket = socket;       // Client's socket
        this.threadList = threads;  // List of clients
    }

    @Override
    public void run() {
        try {
            // Create an Input Stream to read messages from clients
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            /* Let's create a PrintWriter that allows us
               to send messages to clients.
            */
            output = new PrintWriter(socket.getOutputStream(), true);

            // Infinite loop that reads from the client
            while (true) {
                String outputString = input.readLine();
                // Send the message to all clients
                printToAllClients(outputString);
                // Print the message to the console
                // In our case, it won't make sense as it is encrypted
                System.out.println("The server has received: " + outputString);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getStackTrace());
        }
    }

    private void printToAllClients(String outputString) {
        // Send the message to each connected client
        for (ServerThread sT : threadList) {
            sT.output.println(outputString);
        }
    }
}

