import interfaces.EmployeeInterface;
import interfaces.ServerInterface;
import org.opencv.core.Mat;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Server extends UnicastRemoteObject implements ServerInterface {
    private Map<String, String> employees = new HashMap<>();

    private Map<String, Socket> clients = new HashMap<>();
    private static final int CHAT_PORT = 5001;

    static {
//        try {
        SERVER_HOSTNAME = "192.168.43.194";
//        } catch (UnknownHostException e) {
//            throw new RuntimeException(e);
//        }
    }

    public static String SERVER_PORT = "5000";

    // Singleton instance
    private static Server instance;

    // Private constructor to prevent instantiation
    private Server() throws RemoteException {
        super();
    }

    // Method to get the singleton instance
    public static Server getInstance() throws RemoteException {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    @Override
    public void register(EmployeeInterface e) throws RemoteException, UnknownHostException {
        System.out.println(e);
        employees.put(e.getName(),e.getDeviceAddress());
    }

    //    @Override
    public void unregister(String employeeName) throws RemoteException {
        employees.remove(employeeName);
    }

    @Override
    public  Map<String, String> getEmployees()throws RemoteException{
        return this.employees;
    }


    public void registerClient(String username, Socket socket)throws RemoteException {
        clients.put(username, socket);
    }

    public void unregisterClient(String username)throws RemoteException {
        clients.remove(username);
    }

    public Socket getClientSocket(String username)throws RemoteException {
        return clients.get(username);
    }

    public static String SERVER_HOSTNAME;


    public static void main(String[] args) {
        try {
            System.out.println(Server.SERVER_HOSTNAME + ":" + Server.SERVER_PORT);
            System.setProperty("java.rmi.server.hostname", Server.SERVER_HOSTNAME); // Uses the loopback address, 127.0.0.1, if you don't do this.
            Naming.rebind("rmi://" + SERVER_HOSTNAME + ":" + SERVER_PORT + "/server", Server.getInstance());
            System.out.println("Monitoring Server is ready on:" + Server.SERVER_HOSTNAME);

            // Start chat server
            ServerSocket serverSocket = new ServerSocket(CHAT_PORT);
            System.out.println("Chat server started on port " + CHAT_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket, instance).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private Scanner in;
        private Server server;
        private String username;

        public ClientHandler(Socket socket, Server server) {
            this.socket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new Scanner(socket.getInputStream());

                // Register client
                username = in.nextLine();
                server.registerClient(username, socket);
                System.out.println(username + " connected.");

                // Handle incoming messages
                while (in.hasNextLine()) {
                    String message = in.nextLine();
                    System.out.println(username + ": " + message);

                    // Assume the message format is "recipient:message"
                    int colonIndex = message.indexOf(":");
                    if (colonIndex != -1) {
                        String recipient = message.substring(0, colonIndex);
                        String actualMessage = message.substring(colonIndex + 1);

                        // Forward the message to the recipient
                        Socket recipientSocket = server.getClientSocket(recipient);
                        if (recipientSocket != null) {
                            PrintWriter recipientOut = new PrintWriter(recipientSocket.getOutputStream(), true);
                            recipientOut.println(username + ": " + actualMessage);
                        } else {
                            out.println("User " + recipient + " not found.");
                        }
                    } else {
                        out.println("Invalid message format. Use 'recipient:message'");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                System.out.println("in finally");
                try {
                    if (username != null) {
                        System.out.println("remove client from socket list:"+username);
                        server.unregisterClient(username);
                        if(username !=Manager.MANAGER_NAME){
                            System.out.println("remove employee from employees list:"+username);
                            server.unregister(username);
                        }
                    }

                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}