package com.mycompany.simplewebserver;

import com.mycompany.SimpleWebFramework.SimpleWebFramework;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import com.mycompany.SimpleWebFramework.Request;
import com.mycompany.SimpleWebFramework.Response;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

public class SimpleWebServer {

    public static final int PORT = 8080;
    public static final String WEB_ROOT = "target/classes/webroot";

    public static void main(String[] args) throws IOException {
        SimpleWebFramework.get("/hello", (req, res) -> "Hello " + req.getValues("name"));
        SimpleWebFramework.get("/prueba", (req, res) -> "La prueba funciona " + req.getValues("name"));
        SimpleWebFramework.get("/pi", (req, resp) -> {
            return String.valueOf(Math.PI);
        });

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

            if (method.equals("GET")) {
                handleGetRequest(fileRequested, out, dataOut);
            } else if (method.equals("POST")) {
                handlePostRequest(in, out, dataOut);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGetRequest(String fileRequested, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        
        

        if (fileRequested.startsWith("/App/")) {
            fileRequested = fileRequested.replaceFirst("/App", "");
            String[] parts = fileRequested.split("\\?");
            System.out.println("url: " + fileRequested);
            System.out.println("parts: " + parts);
            String path = parts[0];
            String queryString = parts.length > 1 ? parts[1] : "";
            Request req = new Request();
            parseQueryString(queryString, req);

            Response res = new Response();
            String responseBody = SimpleWebFramework.handleGetRequest(path, req, res);

            if (responseBody != null) {
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/plain");
                out.println("Content-Length: " + responseBody.length());
                out.println();
                out.flush();
                dataOut.write(responseBody.getBytes());
                dataOut.flush();
            } else {
                out.println("HTTP/1.1 404 Not Found");
                out.println("Content-Type: text/html");
                out.println();
                out.println("<html><body><h1>File Not Found</h1></body></html>");
                out.flush();
            }
        } else {
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

    }

    private void parseQueryString(String queryString, Request req) {
        if (queryString == null || queryString.isEmpty()) {
            return;
        }
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                req.setParameter(keyValue[0], keyValue[1]);
            }
        }
    }

    private void handlePostRequest(BufferedReader in, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        StringBuilder headers = new StringBuilder();
        StringBuilder postData = new StringBuilder();
        String line;
        int contentLength = 0;
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
        System.out.println("Response:" + jsonResponse);
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + jsonResponse.length());
        out.println();
        out.flush();
        out.println(jsonResponse);
        out.flush();
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
            StringBuilder jsonContent = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }

            System.out.println("JSON Content Read: " + jsonContent.toString());

            JsonElement jsonElement = JsonParser.parseString(jsonContent.toString());
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            JsonArray filteredArray = new JsonArray();

            System.out.println("Parsed JSON Array: " + jsonArray);

            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();
                if (jsonObject.get("name").getAsString().contains(name)) {
                    filteredArray.add(jsonObject);
                }
            }

            jsonResponse.append(filteredArray.toString());
        } catch (IOException e) {
            System.err.println("IO Exception while reading JSON file: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Exception while processing JSON data: " + e.getMessage());
            e.printStackTrace();
        }

        return jsonResponse.toString();
    }
}
