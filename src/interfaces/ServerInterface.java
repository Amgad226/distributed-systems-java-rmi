package interfaces;

import org.opencv.core.Mat;

import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface ServerInterface extends Remote {
    // RMI function
    Map<String, String> getEmployees() throws RemoteException;
    void register(EmployeeInterface employee ) throws RemoteException, UnknownHostException;
    void unregister(String employee ) throws RemoteException, UnknownHostException;


    // Socket function
    Socket getClientSocket(String username)throws RemoteException;;
    void registerClient(String username, Socket socket)throws RemoteException;;
    void unregisterClient(String username)throws RemoteException;;

}
