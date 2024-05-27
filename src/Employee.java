import classes.WebcamCapture;
import interfaces.EmployeeInterface;
import interfaces.ServerInterface;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class Employee extends UnicastRemoteObject implements Serializable, EmployeeInterface {
    String name;

    public Employee(String name) throws RemoteException {
        super();
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public byte[] captureImage() throws RemoteException {
        Mat mat =  WebcamCapture.captureImage(this.name);

        MatOfByte buffer = new MatOfByte();

        Imgcodecs.imencode(".png", mat, buffer);

        return buffer.toArray();
    }

    @Override
    public byte[] captureScreenshot() throws RemoteException {
        try {
            // Capture the screen using Robot class
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenFullImage = robot.createScreenCapture(screenRect);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(screenFullImage, "png", baos);
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            return imageInByte;
        } catch (AWTException | IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String getDeviceAddress() throws RemoteException {
        try {
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
            Scanner reader = new Scanner(System.in);
            System.out.println("Enter your name: ");
            String name = reader.nextLine();
            reader.close();

            EmployeeInterface employee = new Employee(name);
            ServerInterface server = (ServerInterface) Naming.lookup("//192.168.83.1:5000/server");
            server.register(employee);
            System.out.println("Employee registered with name: " + name);


            String ip = getIp() ;
            System.out.println(ip);
            System.setProperty("java.rmi.server.hostname", ip); // Uses the loopback address, 127.0.0.1, if you don't do this.
            Naming.bind("rmi://"+ip+"/employee",employee);
            System.out.println("Monitoring employee is ready on :"+ip);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
