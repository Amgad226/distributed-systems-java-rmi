import interfaces.EmployeeInterface;
import interfaces.ServerInterface;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Server extends UnicastRemoteObject implements ServerInterface {
    List<EmployeeInterface> employees = new ArrayList<EmployeeInterface>();

    protected Server() throws RemoteException {
        super();
    }
    public void register (EmployeeInterface e) throws RemoteException
    {
        employees.add(e);
    }

    @Override
    public List<EmployeeInterface> getEmployees() throws RemoteException {
        return this.employees;
    }

    public static void main(String[] args) {
        try {
            Naming.rebind("//localhost/server", new Server());
            System.out.println("Monitoring Server is ready.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}