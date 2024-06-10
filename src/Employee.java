import classes.Util;
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
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class Employee extends UnicastRemoteObject implements Serializable, EmployeeInterface {
    private static final long serialVersionUID = 1L;
    private String name;
    private Socket chatSocket;
    private PrintWriter out;
    private Scanner in;
    protected Boolean openChet = false;
    private String ip;

    public Employee(String name, String ip) throws RemoteException {
        super();
        this.name = name;
        this.ip = ip;
    }
    @Override
    public String getName() throws RemoteException {
        return this.name;
    }
    @Override
    public String getDeviceAddress() throws RemoteException {
        return this.ip;
    }

    @Override
    public byte[] captureImage() throws RemoteException {
        Mat mat =WebcamCapture.captureImage(this.name);

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
    public void switchOpenChat(Boolean bool) throws RemoteException {
        this.openChet = bool;
    }

    @Override
    public void connectToChatServer(String serverIp, int port, Scanner userInput) throws IOException {
        chatSocket = new Socket(serverIp, port);
        out = new PrintWriter(chatSocket.getOutputStream(), true);
        in = new Scanner(chatSocket.getInputStream());

        // Send username to chat server
        out.println(name);

        // Start a thread to listen for incoming messages
        new Thread(() -> {
            while (in.hasNextLine()) {
                System.out.println(in.nextLine());
            }
        }).start();

        System.out.print("Enter your message (type 'exit' to end chat): ");
        while (userInput.hasNextLine()) {
            String message = userInput.nextLine();

            if (this.openChet == false) {
                System.out.print("The manager does not open the chat,so if you send message its no sended ");
                continue;
            }
            out.println("manager" + ": " + message);

        }
    }

    public static final String SERVER_HOST = "192.168.43.115";
    public static final String RMI_SERVER_PORT = "5000";
    public static final int SOCKET_SERVER_PORT = 4000;
    public static void main(String[] args) {
        try {
            Scanner reader = new Scanner(System.in);
            System.out.println("Enter your name: ");
            String name = reader.nextLine();
            String myIp = Util.getIp();

            EmployeeInterface employee = new Employee(name, myIp);

            ServerInterface server = (ServerInterface) Naming.lookup("//"+SERVER_HOST+":"+RMI_SERVER_PORT+"/server");
            server.register(employee);
            System.out.println("Employee registered with name: " + name);


            System.setProperty("java.rmi.server.hostname", myIp);
            Naming.bind("rmi://" + myIp + "/employee", employee);
            System.out.println("Monitoring employee is ready on :" + myIp);

            //Connect to socket chat server
            employee.connectToChatServer(SERVER_HOST, SOCKET_SERVER_PORT, reader);

        } catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
