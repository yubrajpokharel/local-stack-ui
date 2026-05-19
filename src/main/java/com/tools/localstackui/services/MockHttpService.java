package com.tools.localstackui.services;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MockHttpService {

  private Map<Integer, MockHttpServer> servers = new HashMap<>();

  @Value("${mock.servers:}")
  private String mockServersConfig;

  public void initializeServersFromEnv() throws IOException {
    if (mockServersConfig == null || mockServersConfig.trim().isEmpty()) {
      return;
    }

    String[] serverConfigs = mockServersConfig.split(";");
    for (String config : serverConfigs) {
      String[] parts = config.split("\\|");
      if (parts.length >= 2) {
        try {
          String serverName = parts[0].trim();
          int port = Integer.parseInt(parts[1].trim());
          String response = parts.length > 2 ? parts[2].trim() : "ok";
          startServer(serverName, port, response);
        } catch (NumberFormatException e) {
          System.err.println("Invalid port in config: " + config);
        }
      }
    }
  }

  public synchronized String startServer(int port) throws IOException {
    return startServer("mock-server-" + port, port, "ok");
  }

  public synchronized String startServer(String serverName, int port, String response)
      throws IOException {
    if (servers.containsKey(port)) {
      return "Server already running on port " + port;
    }

    if (!isPortAvailable(port)) {
      return "Port " + port + " is already in use";
    }

    MockHttpServer server = new MockHttpServer(serverName, port, response);
    new Thread(server).start();
    servers.put(port, server);
    return "Mock HTTP server '" + serverName + "' started on port " + port;
  }

  public synchronized String stopServer(int port) {
    MockHttpServer server = servers.remove(port);
    if (server != null) {
      server.stop();
      return "Mock HTTP server stopped on port " + port;
    }
    return "No server running on port " + port;
  }

  public synchronized List<Map<String, Object>> getRunningServers() {
    List<Map<String, Object>> result = new ArrayList<>();
    for (Map.Entry<Integer, MockHttpServer> entry : servers.entrySet()) {
      Map<String, Object> serverInfo = new HashMap<>();
      serverInfo.put("port", entry.getKey());
      serverInfo.put("name", entry.getValue().getServerName());
      serverInfo.put("address", "http://localhost:" + entry.getKey());
      serverInfo.put("response", entry.getValue().getResponseBody());
      serverInfo.put("createdOn", entry.getValue().getCreatedOn().toString());
      serverInfo.put("running", entry.getValue().isRunning());
      result.add(serverInfo);
    }
    return result;
  }

  public synchronized String startMultipleServers(int count, List<Integer> ports) {
    if (ports.size() != count) {
      return "Port count mismatch. Expected " + count + " ports, got " + ports.size();
    }

    StringBuilder result = new StringBuilder();
    for (int port : ports) {
      try {
        String response = startServer(port);
        result.append(response).append("\n");
      } catch (IOException e) {
        result.append("Failed to start server on port ").append(port).append(": ")
            .append(e.getMessage()).append("\n");
      }
    }
    return result.toString();
  }

  public synchronized String stopAllServers() {
    StringBuilder result = new StringBuilder();
    List<Integer> ports = new ArrayList<>(servers.keySet());
    for (int port : ports) {
      result.append(stopServer(port)).append("\n");
    }
    return result.isEmpty() ? "No servers running" : result.toString();
  }

  private boolean isPortAvailable(int port) {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      return true;
    } catch (IOException e) {
      return false;
    }
  }
}
