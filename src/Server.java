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
        employees.add(e);
    }

    @Override
    public Mat captureImage(EmployeeInterface e) throws RemoteException {
        return e.captureImage();
    }

    @Override
    public List<EmployeeInterface> getEmployees() throws RemoteException {
        return this.employees;
    }

    public static void main(String[] args) {
        try {
            // Start the RMI registry programmatically
            LocateRegistry.createRegistry(1099);

            Server server = Server.getInstance();
            String ip = getIp();
            Naming.rebind("rmi://" + ip + "/server", server);

            System.out.println("Monitoring Server is ready at " + ip);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getIp() throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getLocalHost();
        return inetAddress.getHostAddress();
    }
}
