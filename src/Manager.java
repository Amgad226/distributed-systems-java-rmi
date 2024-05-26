import interfaces.EmployeeInterface;
import interfaces.ServerInterface;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteStub;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Manager extends UnicastRemoteObject implements Remote, Serializable {
    private static final long serialVersionUID = 1L;

    protected Manager() throws RemoteException {
        super();
    }

    public static void main(String[] args) {
        try {

            String serverIp = "192.168.112.1";
            ServerInterface server = (ServerInterface) Naming.lookup("rmi://" + serverIp + "/server");

            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    showMenu();

                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    if (choice == 0) {
                        System.out.println("Exiting...");
                        break;
                    }
//                    server = (ServerInterface) Naming.lookup("rmi://192.168.137.1/server");
                    handleUserChoice(server, scanner, choice);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showMenu() {
        System.out.println("---------------------");
        System.out.println("Enter 1 to list all users");
        System.out.println("Enter 2 to capture photo for a user");
        System.out.println("Enter 3 to capture screenshot for a user");
        System.out.println("Enter 4 to test");
        System.out.println("Enter 0 to exit");
        System.out.println("---------------------");
        System.out.print("Your choice: ");
    }

    private static void handleUserChoice(ServerInterface server, Scanner scanner, int choice) throws IOException, NotBoundException {
        switch (choice) {
            case 1:
                listAllUsers(server);
                break;
            case 2:
                captureUserPhoto(server, scanner);
                break;
            case 3:
               captureUserScreenshot(server, scanner);
                break;

            default:
                System.out.println("Invalid choice, please try again.");
        }
    }

    private static void listAllUsers(ServerInterface server) throws RemoteException {
        List<EmployeeInterface> employees = server.getEmployees();
        for (EmployeeInterface employee : employees) {
            System.out.println("Name: " + employee.getName());
        }
    }

    private static void captureUserPhoto(ServerInterface server, Scanner scanner) throws RemoteException {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        EmployeeInterface employee = findEmployeeByName(server, username);
        if (employee == null) {
            System.out.println("Employee not found.");
            return;
        }
        Mat image = employee.captureImage();
        Path path = Paths.get(System.getProperty("user.home"), "Desktop", "distributed_system","capturedImage");
        String randomString =generateRandomString(6);
        String fileName = username +"_"+randomString +".jpg";

        saveImageToFile(image, path,fileName);
        return;
    }

    private static void captureUserScreenshot(ServerInterface server, Scanner scanner) throws IOException, NotBoundException {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        EmployeeInterface employee = findEmployeeByName(server, username);
        if (employee == null) {
            System.out.println("Employee not found.");
            return;
        }
        String employeeIp = employee.getDeviceAddress(); // or localhost
        System.out.println(employeeIp);

        String host = "rmi://" + employeeIp + "/employee" ;
//        String host = "/employee";

        EmployeeInterface rmiEmployee = new Employee("sd"); // FIXME
//        EmployeeInterface rmiEmployee = (EmployeeInterface) Naming.lookup(host);
        System.out.println(rmiEmployee.getName());
        byte[] byteImage = rmiEmployee.captureScreenshot();
        BufferedImage bufferedScreenshot  =convertByteArrayToBufferedImage(byteImage);

        System.out.println(bufferedScreenshot);
        System.out.println("store the bytes in var ");

        Path path = Paths.get(System.getProperty("user.home"), "Desktop", "distributed_system","screenshots");
        String randomString =generateRandomString(6);
        String fileName = rmiEmployee.getName() +"_"+randomString +".jpg";

        saveScreenshotToFile(bufferedScreenshot, path,fileName);

    }

    private static EmployeeInterface findEmployeeByName(ServerInterface server, String username) throws RemoteException {
        System.out.println("findEmployeeByName");
        for (EmployeeInterface employee : server.getEmployees()) {
            if (employee.getName().equalsIgnoreCase(username)) {
                return employee;
            }
        }
        return null;
    }

    private static void saveImageToFile(Mat image,Path path, String filename) {
        File directory = path.toFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }
        String filePath = path.resolve(filename).toString();
        Imgcodecs.imwrite(filePath, image);
        System.out.println("Image saved to " + filePath);
    }

    private static void saveScreenshotToFile(BufferedImage screenshot,Path path, String filename) throws IOException{
        try {
            File directory = path.toFile();
            if (!directory.exists()) {
                directory.mkdirs();
            }
            String filePath = path.resolve(filename).toString();
            System.out.println(filePath);
            System.out.println(screenshot);

            ImageIO.write(screenshot, "png", new File(filePath));
            System.out.println("Screenshot saved to " + filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        // Create a StringBuilder to store the random string
        StringBuilder sb = new StringBuilder();

        // Create a Random object to generate random indices
        Random random = new Random();

        // Generate the random string
        for (int i = 0; i < length; i++) {
            // Get a random index within the range of the characters string
            int randomIndex = random.nextInt(characters.length());

            // Append the character at the random index to the StringBuilder
            sb.append(characters.charAt(randomIndex));
        }

        return sb.toString();
    }
    public static BufferedImage convertByteArrayToBufferedImage(byte[] byteArray) throws IOException {
        System.out.println(byteArray);
        ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
        BufferedImage image =  ImageIO.read(bais);
        if(image == null){
            System.out.println("the converted byte[] to bufferedImage return null ");
        }
        return image ;
    }
}
