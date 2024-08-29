package com.mycompany.SimpleWebFramework;

public class Response {
    private int status = 200; // Código de estado por defecto
    private String body = "";

    public void setStatus(int status) {
        this.status = status;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public int getStatus() {
        return status;
    }
}