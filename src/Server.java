import interfaces.EmployeeInterface;
import interfaces.ServerInterface;
import org.opencv.core.Mat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Server extends UnicastRemoteObject implements ServerInterface {
    private List<EmployeeInterface> employees = new ArrayList<>();

    // Singleton instance
    private static Server instance;

    // Private constructor to prevent instantiation
    private Server() throws RemoteException {
        super();
    }

    // Method to get the singleton instance
    public static synchronized Server getInstance() throws RemoteException {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    @Override
    public void register(EmployeeInterface e) throws RemoteException {
        System.out.println(e);
        employees.add(e);
    }

    @Override
    public List<EmployeeInterface> getEmployees() throws RemoteException {

        return this.employees;
    }
    public static String SERVER_HOSTNAME;

    static {
        try {
            SERVER_HOSTNAME = getIp();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    public static String SERVER_PORT="5000";



    public static void main(String[] args) {
        try {
            // Start the RMI registry programmatically
//            LocateRegistry.createRegistry(1098);
            System.out.println(Server.SERVER_HOSTNAME+":"+Server.SERVER_PORT);
            System.setProperty("java.rmi.server.hostname", Server.SERVER_HOSTNAME); // Uses the loopback address, 127.0.0.1, if you don't do this.
            Naming.rebind("rmi://"+SERVER_HOSTNAME+":"+Server.SERVER_PORT+"/server",Server.getInstance());


            System.out.println("Monitoring Server is ready on:"+ Server.SERVER_HOSTNAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getIp() throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getLocalHost();
        return inetAddress.getHostAddress();
    }
}
