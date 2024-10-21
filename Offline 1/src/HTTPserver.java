import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class HTTPserver {
    private static final int PORT= 5104;
    public static final String ROOT_DIR=".";
    public static final String UPLOAD_DIR="./uploaded";

    public static void main(String[] args) throws IOException{
        ServerSocket serverSocket= new ServerSocket(PORT);
        System.out.println("Server started at port: " + PORT);

        while(true){
            Socket clientSocket = serverSocket.accept();
            new Thread(new ClientHandler(clientSocket)).start();
        }
    }
}


