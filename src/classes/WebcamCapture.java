package classes;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;

import java.io.Serializable;
import java.nio.file.Paths;

public class WebcamCapture implements Serializable {
    static {
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    public static Mat captureImage(String userName) {
        VideoCapture camera = new VideoCapture(0); // Open the default camera

        if (!camera.isOpened()) {
            System.out.println("Error: Camera not found!");
            return null;
        }

        Mat frame = new Mat();
        camera.read(frame); // Capture a frame

        String filename = null;
        Mat resizedImage = null;
        if (!frame.empty()) {
            // Resize the image for better quality (optional)
            resizedImage = new Mat();
            Size size = new Size(640, 480);
            Imgproc.resize(frame, resizedImage, size);

            // Save the captured image to file
//            filename = Paths.get(System.getProperty("user.home"), userName + "_capturedImage.jpg").toString();
//            Imgcodecs.imwrite(filename, resizedImage);
//            System.out.println("Image saved to " + filename);
        }
        camera.release();
        return resizedImage;

//        return filename;
    }
}
