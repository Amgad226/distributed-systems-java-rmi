import classes.Util;
import interfaces.EmployeeInterface;
import interfaces.ServerInterface;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Scanner;

public class Manager extends UnicastRemoteObject implements Remote, Serializable {
    static {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
    }
    private static final long serialVersionUID = 1L;

    protected Manager() throws RemoteException {
        super();
    }

    public static void main(String[] args) {
        try {

            String serverIp = Server.SERVER_HOSTNAME+":"+Server.SERVER_PORT;
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
                    try {
                        handleUserChoice(server, scanner, choice);
                    }catch (ClassNotFoundException e){
                        System.out.println("Exception:"+e.getMessage());
                    }
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
//        System.out.println("Enter 4 to test");
        System.out.println("Enter 0 to exit");
        System.out.println("---------------------");
        System.out.print("Your choice: ");
    }

    private static void handleUserChoice(ServerInterface server, Scanner scanner, int choice) throws Exception {
        switch (choice) {
            case 1:
                printStoredEmployeeNames(server);
                break;
            case 2:
                captureEmployeePhoto(server, scanner);
                break;
            case 3:
               captureEmployeeScreenshot(server, scanner);
                break;

            default:
                System.out.println("Invalid choice, please try again.");
        }
    }

    private static void printStoredEmployeeNames(ServerInterface server) throws RemoteException {
        List<EmployeeInterface> employees = server.getEmployees();
        for (EmployeeInterface employee : employees) {
            System.out.println("Name: " + employee.getName());
        }
    }

    private static void captureEmployeePhoto(ServerInterface server, Scanner scanner) throws Exception {

        EmployeeInterface rmiEmployee =getEmployeeRmiObjectByName(server,scanner);

        byte[] image = rmiEmployee.captureImage();

         // Convert the byte array to a MatOfByte
        Mat matImage= Util.bytesToMat(image);

        Path path = Paths.get(System.getProperty("user.home"), "Desktop", "distributed_system","capturedImage");
        String randomString =Util.generateRandomString(6);
        String fileName = rmiEmployee.getName() +"_"+randomString +".jpg";

        Util.saveImageToFile(matImage, path,fileName);
    }

    private static void captureEmployeeScreenshot(ServerInterface server, Scanner scanner) throws Exception {
        EmployeeInterface rmiEmployee =getEmployeeRmiObjectByName(server,scanner);

        byte[] byteImage = rmiEmployee.captureScreenshot();
        BufferedImage bufferedScreenshot  =Util.convertByteArrayToBufferedImage(byteImage);

        Path path = Paths.get(System.getProperty("user.home"), "Desktop", "distributed_system","screenshots");
        String randomString =Util.generateRandomString(6);
        String fileName = rmiEmployee.getName() +"_"+randomString +".jpg";

        Util.saveScreenshotToFile(bufferedScreenshot, path,fileName);

    }


    // -SECTION Getter
    private static EmployeeInterface getEmployeeRmiObjectByName(ServerInterface server, Scanner scanner) throws Exception {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        EmployeeInterface employee = findEmployeeInListByName(server, username);

        String employeeIp = employee.getDeviceAddress();
        System.out.println(employeeIp);

        String host = "rmi://" + employeeIp + "/employee" ;

        EmployeeInterface rmiEmployee = (EmployeeInterface) Naming.lookup(host);
        return rmiEmployee;
    }
    private static EmployeeInterface findEmployeeInListByName(ServerInterface server, String username) throws ClassNotFoundException, RemoteException {

        for (EmployeeInterface employee : server.getEmployees()) {
            if (employee.getName().equalsIgnoreCase(username)) {
                return employee;
            }
        }
        throw new ClassNotFoundException("Employee not found in server list.");
    }
}
