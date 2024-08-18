package com.mycompany.simplewebserver;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class SimpleWebServer {

    public static final int PORT = 8080;
    public static final String WEB_ROOT = "target/classes/webroot";

    public static void main(String[] args) throws IOException {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        ServerSocket serverSocket = new ServerSocket(PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            threadPool.submit(new ClientHandler(clientSocket));
        }
    }
}

class ClientHandler implements Runnable {

    public Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true); BufferedOutputStream dataOut = new BufferedOutputStream(clientSocket.getOutputStream())) {

            String requestLine = in.readLine();
            if (requestLine == null) {
                return;
            }

            String[] tokens = requestLine.split(" ");
            String method = tokens[0];
            String fileRequested = tokens[1];

            //printRequestHeader(requestLine ,in);
            if (method.equals("GET")) {
                handleGetRequest(fileRequested, out, dataOut);
            } else if (method.equals("POST")) {
                System.out.println("entra post");
                handlePostRequest(in, out, dataOut);
            }

        } catch (IOException e) {
        }
    }

    private void handleGetRequest(String fileRequested, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        File file = new File(SimpleWebServer.WEB_ROOT, fileRequested);
        int fileLength = (int) file.length();
        String content = getContentType(fileRequested);

        if (file.exists()) {
            byte[] fileData = readFileData(file, fileLength);
            out.println("HTTP/1.1 200 OK");
            System.out.println("Content-type: " + content);
            out.println("Content-type: " + content);
            out.println("Content-length: " + fileLength);
            out.println();
            out.flush();
            dataOut.write(fileData, 0, fileLength);
            dataOut.flush();
        } else {
            out.println("HTTP/1.1 404 Not Found");
            out.println("Content-type: text/html");
            out.println();
            out.flush();
            out.println("<html><body><h1>File Not Found</h1></body></html>");
            out.flush();
        }
    }

    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".html")) {
            return "text/html";
        } else if (fileRequested.endsWith(".css")) {
            return "text/css";
        } else if (fileRequested.endsWith(".js")) {
            return "application/javascript";
        } else if (fileRequested.endsWith(".png")) {
            return "image/png";
        } else if (fileRequested.endsWith(".jpg")) {
            return "image/jpeg";
        } else if (fileRequested.endsWith(".json")) {
            return "application/json";
        }
        return "text/plain";
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];
        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null) {
                fileIn.close();
            }
        }
        return fileData;
    }

    private void printRequestHeader(String requestLine, BufferedReader in) throws IOException {
        System.out.println("Request line:" + requestLine);
        String inputLine = "";
        while ((inputLine = in.readLine()) != null) {
            if (!in.ready()) {
                break;
            }
        }
    }

    private void handlePostRequest(BufferedReader in, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        StringBuilder headers = new StringBuilder();
        StringBuilder postData = new StringBuilder();
        String line;
        int contentLength = 0;
        //System.out.println("Request line post:"+in);
        //System.out.println("Request out  line post:"+out);
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            headers.append(line).append("\n");
            if (line.isEmpty()) {
                break;
            }
            if (line.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }
        }
        System.out.println("Headers received: " + headers.toString());

        if (contentLength > 0) {
            char[] body = new char[contentLength];
            in.read(body, 0, contentLength);
            postData.append(body);
        }
        System.out.println("POST data received: " + postData.toString());

        String name = parseNameFromPostData(postData.toString());
        System.out.println("nombre: " + name);
        String jsonResponse = filterJsonData(name);

        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + jsonResponse.length());
        out.println();
        out.flush();
        System.out.println("Response:" + jsonResponse);
        out.println(jsonResponse);
        out.flush();
    }

    private String parseNameFromPostData(String postData) {
        String[] parts = postData.split("&");
        for (String part : parts) {
            if (part.startsWith("name=")) {
                return part.substring("name=".length());
            }
        }
        return "";
    }

    private String filterJsonData(String name) {
        File jsonFile = new File(SimpleWebServer.WEB_ROOT, "drivers.json");
        StringBuilder jsonResponse = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(jsonFile))) {
            String line;
            StringBuilder jsonContent = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }

            JSONArray jsonArray = new JSONArray(jsonContent.toString());
            JSONArray filteredArray = new JSONArray();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString("name").contains(name)) {
                    filteredArray.put(jsonObject);
                }
            }
            jsonResponse.append(filteredArray.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonResponse.toString();
    }

}
