import classes.Util;
import interfaces.EmployeeInterface;
import interfaces.ServerInterface;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class Manager extends UnicastRemoteObject {
    static {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

    }
    static String MANAGER_NAME = "manager";
    private static final long serialVersionUID = 1L;
    private Socket chatSocket;
    private PrintWriter out;
    private Scanner in;
    private Scanner userInput;
    private ServerInterface server;

    protected Manager(ServerInterface server) throws RemoteException {
        super();
        this.server = server;
    }

    public static void main(String[] args) {
        try {
            String serverIp = "localhost:5000"; // Update with actual server IP if needed
            ServerInterface server = (ServerInterface) Naming.lookup("rmi://" + serverIp + "/server");

            Manager manager = new Manager(server);
            manager.connectToChatServer("localhost", 5001); // Update with actual chat server IP if needed

            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    manager.showMenu();
                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    if (choice == 0) {
                        System.out.println("Exiting...");
                        break;
                    }
                    try {
                        manager.handleUserChoice(scanner, choice);
                    } catch (ClassNotFoundException e) {
                        System.out.println("Exception:" + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connectToChatServer(String serverIp, int port) throws IOException {
        chatSocket = new Socket(serverIp, port);
        out = new PrintWriter(chatSocket.getOutputStream(), true);
        in = new Scanner(chatSocket.getInputStream());
        userInput = new Scanner(System.in);

        // Register username
        System.out.print("HI "+MANAGER_NAME);
        out.println(MANAGER_NAME);

        // Start a thread to listen for incoming messages
        new Thread(() -> {
            while (in.hasNextLine()) {
                System.out.println( in.nextLine());
            }
        }).start();
    }

    private void showMenu() {
        System.out.println("---------------------");
        System.out.println("Enter 1 to list all users");
        System.out.println("Enter 2 to capture photo for a user");
        System.out.println("Enter 3 to capture screenshot for a user");
        System.out.println("Enter 4 to chat with a user");
        System.out.println("Enter 0 to exit");
        System.out.println("---------------------");
        System.out.print("Your choice: ");
    }

    private void handleUserChoice(Scanner scanner, int choice) throws Exception {
        switch (choice) {
            case 1:
                printStoredEmployeeNames();
                break;
            case 2:
                captureEmployeePhoto(scanner);
                break;
            case 3:
                captureEmployeeScreenshot(scanner);
                break;
            case 4:
                chatWithEmployee(scanner);
                break;
            default:
                System.out.println("Invalid choice, please try again.");
        }
    }

    private void printStoredEmployeeNames() throws RemoteException {
        Map<String, String> employeesMap = server.getEmployees();
        List<String> employeesKeys = Util.mapKeysToList(employeesMap);
        for (String employee : employeesKeys) {
            System.out.println("Name: " +employee);
        }
    }

    private void captureEmployeePhoto(Scanner scanner) throws Exception {
        EmployeeInterface rmiEmployee = getEmployeeRmiObjectByName(scanner);
        byte[] image = rmiEmployee.captureImage();

        // Convert the byte array to a MatOfByte
        Mat matImage = Util.bytesToMat(image);

        Path path = Paths.get(System.getProperty("user.home"), "Desktop", "distributed_system", "capturedImage");
        String randomString = Util.generateRandomString(6);
        String fileName = rmiEmployee.getName() + "_" + randomString + ".jpg";

        Util.saveImageToFile(matImage, path, fileName);
    }

    private void captureEmployeeScreenshot(Scanner scanner) throws Exception {
        EmployeeInterface rmiEmployee = getEmployeeRmiObjectByName(scanner);
        byte[] byteImage = rmiEmployee.captureScreenshot();
        BufferedImage bufferedScreenshot = Util.convertByteArrayToBufferedImage(byteImage);

        Path path = Paths.get(System.getProperty("user.home"), "Desktop", "distributed_system", "screenshots");
        String randomString = Util.generateRandomString(6);
        String fileName = rmiEmployee.getName() + "_" + randomString + ".jpg";

        Util.saveScreenshotToFile(bufferedScreenshot, path, fileName);
    }

    private void chatWithEmployee(Scanner scanner) throws Exception {
        EmployeeInterface rmiEmployee = getEmployeeRmiObjectByName(scanner);
        rmiEmployee.switchOpenChat(true);
        System.out.print("Enter your message (type 'exit' to end chat): ");

        while (true) {
//            System.out.print("you:");
            String message = scanner.nextLine();

            if ("".equalsIgnoreCase(message)) {
                continue;
            }
            if ("exit".equalsIgnoreCase(message)) {
                rmiEmployee.switchOpenChat(false);
                break;
            }
            out.println(rmiEmployee.getName() + ":" + message);
        }
    }

    private EmployeeInterface getEmployeeRmiObjectByName(Scanner scanner) throws Exception {
        System.out.print("Enter the username of the employee you want:");
        String username = scanner.nextLine();

        String employeeIp = findEmployeeIpByName(username);
        System.out.println("connect on "+username+":"+employeeIp);

        String host = "rmi://" + employeeIp + "/employee";
        return (EmployeeInterface) Naming.lookup(host);
    }

    private String findEmployeeIpByName(String username) throws ClassNotFoundException, RemoteException {
        Map<String, String> employees =server.getEmployees() ;
        String employeeIp=  employees.get(username);
        if(employeeIp==null){
            throw new ClassNotFoundException("Employee not found in server list.");
        }
        return employeeIp;
    }
}
