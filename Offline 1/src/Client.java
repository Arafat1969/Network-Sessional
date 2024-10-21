import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 5104;
    private static final int CHUNK_SIZE = 128;

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newCachedThreadPool();

        while (true) {

            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

            try {
                String command = input.readLine();
                String fileName = command.split(" ")[1];
                String filePath= "./"+fileName;
                File file = new File(filePath);

                if (file.exists() && isValidFileType(file)) {
                    threadPool.execute(() -> uploadFile(file));
                } else {
                    System.out.println("Invalid file format. Only text (.txt) and image (.jpg, .png) files are allowed.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {

            }
        }
    }


    private static void uploadFile(File file) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
             BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(file))) {


            String uploadHeader = "UPLOAD " + file.getName() + "\r\n";
            out.write(uploadHeader.getBytes());
            out.flush();


            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            while ((bytesRead = fileIn.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            out.flush();
            System.out.println("File " + file.getName() + " uploaded successfully.");
        } catch (IOException e) {
            System.out.println("Error during file upload: " + e.getMessage());
        }
    }


    private static boolean isValidFileType(File file) {
        try {
            String mimeType = Files.probeContentType(file.toPath());
            return mimeType != null ;//&& (mimeType.startsWith("text") || mimeType.startsWith("image"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}

