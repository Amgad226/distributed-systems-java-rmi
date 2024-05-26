package interfaces;

import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface EmployeeInterface extends Remote {
    Mat  captureImage() throws RemoteException;
    BufferedImage captureScreenshot() throws RemoteException;
    String getDeviceAddress() throws RemoteException;
    String getName() throws RemoteException;
}