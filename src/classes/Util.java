package classes;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

public class Util {
    public static Mat bytesToMat(byte[] byteArray) {
        // Convert the byte array to a MatOfByte
        MatOfByte matOfByte = new MatOfByte(byteArray);
        // Decode the MatOfByte to a Mat
        return Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_UNCHANGED);
    }

    public static void saveImageToFile(Mat image, Path path, String filename) {
        File directory = path.toFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }
        String filePath = path.resolve(filename).toString();
        Imgcodecs.imwrite(filePath, image);
        System.out.println("Image saved to " + filePath);
    }

    public static void saveScreenshotToFile(BufferedImage screenshot, Path path, String filename) throws IOException {
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

    public static String generateRandomString(int length) {
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
