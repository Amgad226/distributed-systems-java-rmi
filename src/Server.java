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

    public static void main(String[] args) {
        try {
            // Start the RMI registry programmatically
//            LocateRegistry.createRegistry(1098);

            System.setProperty("java.rmi.server.hostname", "192.168.137.1"); // Uses the loopback address, 127.0.0.1, if you don't do this.
            Naming.rebind("rmi://192.168.137.1:5000/server",Server.getInstance());

//            Server server = Server.getInstance();
//            String ip = "192.168.137.1:5000";
//            System.out.println(ip);
//            Naming.rebind("/server", server);

            System.out.println("Monitoring Server is ready");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getIp() throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getLocalHost();
        return inetAddress.getHostAddress();
    }
}
