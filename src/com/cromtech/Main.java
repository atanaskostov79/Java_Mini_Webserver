package com.cromtech;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Date;
import java.util.StringTokenizer;

public class Main implements Runnable{
    static final File WEB_ROOT = new File("./html"); //Default web dir.
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";

    private static int port = 8000;
    private  Socket connect;

    public Main(Socket c) {
        connect = c;
    }

    public static void main(String[] args) throws IOException {
	// write your code here
        try {

            ServerSocket serverConnect = new ServerSocket(port);
            while (true){
                Main myServer = new Main(serverConnect.accept());

            Thread thread = new Thread( myServer);
            thread.start();

            }
        }
        catch (EOFException e ){
            System.err.println("Server error : " + e.getMessage());
        }



    }

    @Override
    public void run() {

        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream  dataOut = null;
        String fileReq = null;
        BufferedReader getPost = null;

        try{
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            out = new PrintWriter(connect.getOutputStream());
            dataOut = new BufferedOutputStream(connect.getOutputStream());

            String input = in.readLine();
            StringTokenizer parse = new StringTokenizer(input);
            String method = parse.nextToken().toUpperCase();
            fileReq  = parse.nextToken().toLowerCase();

            if (!method.equals("GET")  &&  !method.equals("HEAD") &&  !method.equals("POST")) {

                File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
                int fileLength = (int) file.length();
                String contentMimeType = "text/html";
                //read content to return to client
                byte[] fileData = readFileData(file, fileLength);

                // we send HTTP Headers with data to client
                out.println("HTTP/1.1 501 Not Implemented");
                out.println("Server: Java HTTP Server from AKostov : 1.0");
                out.println("Date: " + new Date());
                out.println("Content-type: " + contentMimeType);
                out.println("Content-length: " + fileLength);
                out.println();
                out.flush();
                // file
                dataOut.write(fileData, 0, fileLength);
                dataOut.flush();
            } else {
                // GET or HEAD method
                if (fileReq.endsWith("/")) {
                    fileReq += DEFAULT_FILE;
                }
                System.out.println("File " + WEB_ROOT + fileReq);
                File file = new File(WEB_ROOT, fileReq);
                int fileLength = (int) file.length();
                String content = getContentType(fileReq);

                if (method.equals("GET")) {
                    byte[] fileData = readFileData(file, fileLength);

                    // send HTTP Headers
                    out.println("HTTP/1.1 200 OK");
                    out.println("Server: Java HTTP Server from AKostov : 1.0");
                    out.println("Date: " + new Date());
                    out.println("Content-type: " + content);
                    out.println("Content-length: " + fileLength);
                    out.println();
                    out.flush();

                    dataOut.write(fileData, 0, fileLength);
                    dataOut.flush();
                }

                if (method.equals("POST")) {
                   
                    String line;
                    int postDataI = -1;
                    //Read post Header
                    while ((line = in.readLine()) != null && (line.length() != 0)) {

                        if (line.indexOf("Content-Length:") > -1) {
                            postDataI = new Integer(
                                    line.substring(
                                            line.indexOf("Content-Length:") + 16,
                                            line.length())).intValue();
                        }
                    }

                    // read the post data
                    String postData = "";
                    if (postDataI > 0) {
                        char[] charArray = new char[postDataI];
                        in.read(charArray, 0, postDataI);
                        postData = new String(charArray);
                    }

                  

                    content = "text/plain";
                    // send HTTP Headers
                    out.println("HTTP/1.1 200 OK");
                    out.println("Server: Java HTTP Server from AKostov : 1.0");
                    out.println("Date: " + new Date());
                    out.println("Content-type: " + content);
                    out.println("Content-length: " + postData.length());
                    out.println();
                    out.flush();

                    dataOut.flush();
                   
                  
                }

            }

        }catch (FileNotFoundException fnfe) {
            try {
                fileNotFound(out, dataOut, fileReq);
            } catch (IOException ioe) {
                System.err.println("Error with file not found exception : " + ioe.getMessage());
            }

        } catch (IOException ioe) {
            System.err.println("Server error : " + ioe);
        } finally {
            try {
                in.close();
                out.close();
                dataOut.close();
                connect.close(); // we close socket connection
            } catch (Exception e) {
                System.err.println("Error closing stream : " + e.getMessage());
            }


        }
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }

        return fileData;
    }

    // return supported MIME Types
    private String getContentType(String fileReq) {
        System.out.println(fileReq);
        String content = "text/plain";
        if (fileReq.endsWith(".htm")  ||  fileReq.endsWith(".html") || fileReq.endsWith(".HTM") ) {
            content = "text/html";
        }
        if (fileReq.endsWith(".css")) {
            content ="text/css";
        }
        if (fileReq.endsWith(".js")) {
            content ="text/javascript";
        }
        if (fileReq.endsWith(".gif")) {
            content ="image/gif";
        }
        return content;
    }

    private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileReq) throws IOException {
        File file = new File(WEB_ROOT, FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        String content = "text/html";
        byte[] fileData = readFileData(file, fileLength);

        out.println("HTTP/1.1 404 File Not Found");
        out.println("Server: Java HTTP Server from AKostov : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-type: " + content);
        out.println("Content-length: " + fileLength);
        out.println(); 
        out.flush(); 

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();

    }
    public static void openWebpage(String urlString) {
        try {
            Desktop.getDesktop().browse(new URL(urlString).toURI());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
