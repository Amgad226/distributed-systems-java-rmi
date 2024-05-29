package interfaces;

import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Scanner;

public interface EmployeeInterface extends Remote {
    byte[]  captureImage() throws RemoteException;
    byte[] captureScreenshot() throws RemoteException;
    String getDeviceAddress() throws RemoteException, UnknownHostException;
    String getName() throws RemoteException;
    void switchOpenChat(Boolean bool) throws RemoteException;


    void connectToChatServer(String serverIp, int port, Scanner reader) throws IOException;
}