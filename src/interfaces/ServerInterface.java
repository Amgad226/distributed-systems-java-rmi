package interfaces;

import org.opencv.core.Mat;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ServerInterface extends Remote {
    void register(EmployeeInterface employee ) throws RemoteException;

    Mat captureImage(EmployeeInterface e) throws RemoteException;

    List<EmployeeInterface> getEmployees() throws RemoteException;
}
