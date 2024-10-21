import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class ClientHandler implements Runnable{

    private Socket clientSocket;
    private static final int CHUNK_SIZE = 32;
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try{
            handleRequest(clientSocket);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRequest(Socket clientSocket) throws  IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter pr= new PrintWriter(clientSocket.getOutputStream(),true);
        BufferedOutputStream out= new BufferedOutputStream(clientSocket.getOutputStream());

        String requestLine = reader.readLine();
        if(requestLine== null || !requestLine.startsWith("GET")){
            if (requestLine != null && requestLine.startsWith("UPLOAD")) {
                String fileName = requestLine.split(" ")[1];
                File uploadDir = new File(HTTPserver.UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                File uploadedFile = new File(uploadDir, fileName);

                try (BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(uploadedFile));
                     BufferedInputStream in = new BufferedInputStream(clientSocket.getInputStream())) {

                    byte[] buffer = new byte[CHUNK_SIZE];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        fileOut.write(buffer, 0, bytesRead);
                    }
                    fileOut.flush();
                    System.out.println("File " + fileName + " uploaded successfully.");
                }
            } else {
                send404Error(pr);
            }
        }

        if(requestLine!=null){
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length != 3) {
                send404Error(pr);
                return;
            }

            String path= requestParts[1];
            System.out.println(path);

            File file= new File(HTTPserver.ROOT_DIR,path);

            if(!file.exists()){
                send404Error(pr);
            }else if (file.isDirectory()) {
                sendDirectoryListing(pr, file,path);
            } else {
                sendFile(pr,out, file);
            }
        }
    }

    private void send404Error(PrintWriter pr ) {
        pr.println("HTTP/1.0 404 Not Found");
        pr.println("Content-Type: text/html");
        pr.println();
        pr.println("<html><body><h1>404: Page Not Found</h1></body></html>");
    }

    private void sendDirectoryListing(PrintWriter pr, File directory, String parentPath){
        pr.println("HTTP/1.0 200 OK");
        pr.println("Content-Type : text/html");
        pr.println();
        //System.out.println("parent :"+ parentPath.length());
        pr.println("<html><body><h1>Directory Listing</h1><ul>");
        for (File file : directory.listFiles()){
            String displayName = file.getName();
            String filePath;
           if(parentPath.length()==1){
               filePath = parentPath + displayName;
           }else{
               filePath = parentPath + "/"+displayName;
           }
            //System.out.println(filePath);
            if(file.isDirectory()){
                displayName= "<b><i>"+displayName+"</i> </b>";
            }
            pr.println("<li><a href=\"" + filePath + "\">" + displayName + "</a></li>");
        }
        pr.println("</ul></body></html>");
    }

    private static void sendFile(PrintWriter pr, BufferedOutputStream out, File file) {
        String mimeType = null;
        try {
            mimeType = Files.probeContentType(file.toPath());
        } catch(IOException e) {
            e.printStackTrace();
        }
        if(mimeType == null)
        {
            mimeType = "application/octet-stream";
        }


        if (mimeType.startsWith("text") || mimeType.startsWith("image")) {

            pr.write("HTTP/1.0 200 OK\r\n");
            pr.write("Content-Type: text/html\r\n");
            pr.write("\r\n");
            pr.flush();

            pr.println("<html><h1>File Content</h1><body>");

            if (mimeType.startsWith("text")) {
                try{
                    pr.println("<pre><b>");
                    BufferedReader fileReader = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = fileReader.readLine()) != null) {
                        pr.println(line);
                    }
                    pr.println("</b></pre>");
                    fileReader.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            } else if (mimeType.startsWith("image")) {

                pr.write("<img src=\"data:" + mimeType + ";base64,");

                try (FileInputStream fileIn = new FileInputStream(file)) {
                    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                    byte[] buffer = new byte[CHUNK_SIZE];
                    int bytesRead;
                    while ((bytesRead = fileIn.read(buffer)) != -1) {
                        byteOut.write(buffer, 0, bytesRead);
                    }
                    String base64Image = Base64.getEncoder().encodeToString(byteOut.toByteArray());
                    pr.write(base64Image);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                pr.write("\" alt=\"Image\" />");
            }

            pr.println("</body></html>");
            pr.flush();
        } else {

            pr.write("HTTP/1.0 200 OK\r\n");
            pr.write("Server: Java HTTP Server: 1.0\r\n");
            pr.write("Date: " + new Date() + "\r\n");
            pr.write("Content-Type: " + mimeType + "\r\n");
            pr.write("Content-Length: " + file.length() + "\r\n");
            pr.write("Content-Disposition: attachment; filename=\"" + file.getName() + "\"\r\n");
            pr.write("\r\n");
            pr.flush();


            try (FileInputStream fileIn = new FileInputStream(file)) {
                byte[] buffer = new byte[CHUNK_SIZE];
                int bytesRead;
                while ((bytesRead = fileIn.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }
}
