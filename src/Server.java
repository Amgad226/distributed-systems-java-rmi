import interfaces.EmployeeInterface;
import interfaces.ServerInterface;
import org.opencv.core.Mat;

import javax.management.remote.rmi.RMIServer;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.rmi.*;
import java.rmi.server.*;
import javax.naming.*;
public class Server extends UnicastRemoteObject implements ServerInterface {
    List<EmployeeInterface> employees = new ArrayList<EmployeeInterface>();

    protected Server() throws RemoteException {
        super();
    }
    public void register (EmployeeInterface e) throws RemoteException
    {
        employees.add(e);
    }
    public Mat captureImage (EmployeeInterface e) throws RemoteException
    {
        return e.captureImage();
    }

    @Override
    public List<EmployeeInterface> getEmployees() throws RemoteException {

        return this.employees;
    }

    public static void main(String[] args) {
        try {
//            System.setProperty("java.rmi.server.hostname", "192.168.137.1"); // Uses the loopback address, 127.0.0.1, if you don't do this.


            Naming.rebind("/server", new Server());
            System.out.println("Monitoring Server is ready.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}