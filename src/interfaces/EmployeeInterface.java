package interfaces;

import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface EmployeeInterface extends Remote {
    byte[]  captureImage() throws RemoteException;
    byte[] captureScreenshot() throws RemoteException;
    String getDeviceAddress() throws RemoteException;
    String getName() throws RemoteException;
}