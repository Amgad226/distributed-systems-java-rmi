import classes.WebcamCapture;
import interfaces.EmployeeInterface;
import interfaces.ServerInterface;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteStub;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;


public class Employee extends UnicastRemoteObject implements  Serializable,EmployeeInterface  {
    String name;

    public Employee(String name) throws RemoteException  {
        super();
        this.name=name;
    }

    @Override
    public String getName(){
        return this.name;
    }

    @Override
    public Mat captureImage() throws RemoteException {
        return WebcamCapture.captureImage(this.name);
    }

    @Override
    public byte[] captureScreenshot() throws RemoteException {
        try {
            // Capture the screen using Robot class
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
//            return robot.createScreenCapture(screenRect);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
////            ImageIO.write(screenFullImage, "png", baos);
////            baos.flush();
            byte[] imageInByte = baos.toByteArray();
////            baos.close();
            return imageInByte;
        } catch (AWTException ex) {
            ex.printStackTrace();
            return  null;
        }
    }

    @Override
    public String getDeviceAddress() throws RemoteException {
        try{
            return getIp();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    private static String getIp() throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getLocalHost();
        return inetAddress.getHostAddress();

    }
    public static void main(String[] args) {
        try {
//
            String name= "name";
            EmployeeInterface employee = new Employee(name);

            System.out.println("Client registered as " + name);
             Naming.rebind("/employee", employee);

            System.out.println("Monitoring Employee is ready....");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());;
        }
    }
}








