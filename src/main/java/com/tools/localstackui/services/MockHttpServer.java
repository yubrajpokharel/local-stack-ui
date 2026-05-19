package com.tools.localstackui.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class MockHttpServer implements Runnable {

  private String serverName;
  private int port;
  private String responseBody;
  private ServerSocket serverSocket;
  private volatile boolean running = false;
  private Instant createdOn;

  public MockHttpServer(int port) throws IOException {
    this("mock-server-" + port, port, "ok");
  }

  public MockHttpServer(String serverName, int port, String responseBody) throws IOException {
    this.serverName = serverName;
    this.port = port;
    this.responseBody = responseBody;
    this.serverSocket = new ServerSocket(port);
    this.running = true;
    this.createdOn = Instant.now();
  }

  @Override
  public void run() {
    while (running) {
      try {
        Socket clientSocket = serverSocket.accept();
        new Thread(new ClientHandler(clientSocket, port, responseBody)).start();
      } catch (IOException e) {
        if (running) {
          e.printStackTrace();
        }
      }
    }
  }

  public void stop() {
    running = false;
    try {
      if (serverSocket != null && !serverSocket.isClosed()) {
        serverSocket.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public boolean isRunning() {
    return running;
  }

  public String getServerName() {
    return serverName;
  }

  public int getPort() {
    return port;
  }

  public String getResponseBody() {
    return responseBody;
  }

  public Instant getCreatedOn() {
    return createdOn;
  }

  private static class ClientHandler implements Runnable {

    private Socket socket;
    private int port;
    private String responseBody;

    public ClientHandler(Socket socket, int port, String responseBody) {
      this.socket = socket;
      this.port = port;
      this.responseBody = responseBody;
    }

    @Override
    public void run() {
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
          OutputStream output = socket.getOutputStream()) {

        String requestLine = reader.readLine();
        if (requestLine == null) {
          return;
        }

        String[] parts = requestLine.split(" ");
        String method = parts[0];
        String path = parts.length > 1 ? parts[1] : "/";

        String response = responseBody;
        String contentType = isJsonResponse(response) ? "application/json" : "text/plain";
        String httpResponse = "HTTP/1.1 200 OK\r\n"
            + "Content-Type: " + contentType + "\r\n"
            + "Content-Length: " + response.getBytes(StandardCharsets.UTF_8).length + "\r\n"
            + "Connection: close\r\n"
            + "\r\n"
            + response;

        output.write(httpResponse.getBytes(StandardCharsets.UTF_8));
        output.flush();
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          socket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    private boolean isJsonResponse(String response) {
      return response != null && response.trim().startsWith("{");
    }
  }
}
