import interfaces.EmployeeInterface;
import interfaces.ServerInterface;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Server extends UnicastRemoteObject implements ServerInterface {
    // to store name and ip
    private Map<String, String> employees = new HashMap<>();

    // to store name and his socket instance
    private Map<String, Socket> clients = new HashMap<>();
    public static final int SOCKET_SERVER_PORT = 4000;
    public static final String SERVER_HOSTNAME = "192.168.43.194";
    public static final String RMI_SERVER_PORT = "5000";

    private static Server instance;
    private Server() throws RemoteException {
        super();
    }
    public static Server getInstance() throws RemoteException {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    // RMI function
    @Override
    public void register(EmployeeInterface e) throws RemoteException, UnknownHostException {
        System.out.println(e);
        employees.put(e.getName(),e.getDeviceAddress());
    }
    @Override
    public void unregister(String employeeName) throws RemoteException {
        employees.remove(employeeName);
    }
    @Override
    public  Map<String, String> getEmployees()throws RemoteException{
        return this.employees;
    }


    // Socket functions
    @Override
    public void registerClient(String username, Socket socket)throws RemoteException {
        clients.put(username, socket);
    }
    @Override
    public void unregisterClient(String username)throws RemoteException {
        clients.remove(username);
    }
    @Override
    public Socket getClientSocket(String username)throws RemoteException {
        return clients.get(username);
    }


    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", Server.SERVER_HOSTNAME);
            Naming.rebind("rmi://" + SERVER_HOSTNAME + ":" + RMI_SERVER_PORT + "/server", Server.getInstance());
            System.out.println("Monitoring Server is ready on:" +Server.SERVER_HOSTNAME + ":" + Server.RMI_SERVER_PORT);

            // Start chat server
            ServerSocket serverSocket = new ServerSocket(SOCKET_SERVER_PORT);
            System.out.println("Chat server started on port " + SOCKET_SERVER_PORT);

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