package Server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
/*
The following code is the main server application.
Sources: https://github.com/gyawaliamit7/MultiClientSocket
*/

public class ServerMain {

    public static void main(String[] args) {

        // Create a list for all our clients
        ArrayList<ServerThread> threadList = new ArrayList<>();
        // The server socket will open on port 5000
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            while (true) {
                // Whenever a new client arrives, accept the connection
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                /* After accepting the connection, open a thread for that client,
                   this way each client has a running thread,
                   allowing all clients to send and receive messages simultaneously
                */
                ServerThread serverThread = new ServerThread(socket, threadList);
                // Add the new thread for this client to the list of all clients
                threadList.add(serverThread);
                // When we execute the start, we initiate the run of the ServerThread
                // That is, we start receiving and sending messages
                serverThread.start();
            }
        } catch (Exception e) {
            System.out.println("Error occurred in main: " + e.getStackTrace());
        }
    }
    
}

