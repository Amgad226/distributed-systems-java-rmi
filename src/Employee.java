import classes.WebcamCapture;
import interfaces.EmployeeInterface;
import interfaces.ServerInterface;
import org.opencv.core.Mat;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Scanner;

public class Employee  implements EmployeeInterface,Serializable  {
    String name;

    public Employee(String name)  {
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
    public BufferedImage captureScreenshot() throws RemoteException {
        try {
            // Capture the screen using Robot class
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            return robot.createScreenCapture(screenRect);
        } catch (AWTException ex) {
            ex.printStackTrace();
            return  null;
        }
    }

    @Override
    public String getDeviceAddress() throws RemoteException {
        try{
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) {
        try {
            ServerInterface server = (ServerInterface) Naming.lookup("//localhost/server");
            Scanner reader = new Scanner(System.in);
            System.out.println("Enter your name: ");
            String name = reader.nextLine();
            reader.close();

            EmployeeInterface employee = new Employee(name);
            server.register(employee);

            System.out.println("Client registered as " + name);

        } catch (Exception e) {
            System.out.println(e.getMessage());;
        }
    }
}