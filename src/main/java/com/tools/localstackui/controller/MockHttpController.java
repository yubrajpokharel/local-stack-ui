package com.tools.localstackui.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.tools.localstackui.services.MockHttpService;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockHttpController {

  @Autowired
  MockHttpService mockHttpService;

  @PostMapping(value = "/mock-http/start/{port}", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> startServer(@PathVariable("port") int port) {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("message", mockHttpService.startServer(port));
      response.put("status", "success");
    } catch (IOException e) {
      response.put("message", "Failed to start server: " + e.getMessage());
      response.put("status", "error");
    }
    return response;
  }

  @PostMapping(value = "/mock-http/start", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> startServerWithConfig(
      @RequestParam("name") String serverName,
      @RequestParam("port") int port,
      @RequestParam(value = "response", defaultValue = "ok") String responseBody) {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("message", mockHttpService.startServer(serverName, port, responseBody));
      response.put("status", "success");
    } catch (IOException e) {
      response.put("message", "Failed to start server: " + e.getMessage());
      response.put("status", "error");
    }
    return response;
  }

  @DeleteMapping(value = "/mock-http/stop/{port}", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> stopServer(@PathVariable("port") int port) {
    Map<String, String> response = new LinkedHashMap<>();
    response.put("message", mockHttpService.stopServer(port));
    response.put("status", "success");
    return response;
  }

  @GetMapping(value = "/mock-http/servers", produces = APPLICATION_JSON_VALUE)
  public List<Map<String, Object>> getRunningServers() {
    return mockHttpService.getRunningServers();
  }

  @PostMapping(value = "/mock-http/start-multiple", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> startMultipleServers(
      @RequestParam("count") int count,
      @RequestParam("ports") List<Integer> ports) {
    Map<String, String> response = new LinkedHashMap<>();
    response.put("message", mockHttpService.startMultipleServers(count, ports));
    response.put("status", "success");
    return response;
  }

  @DeleteMapping(value = "/mock-http/stop-all", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> stopAllServers() {
    Map<String, String> response = new LinkedHashMap<>();
    response.put("message", mockHttpService.stopAllServers());
    response.put("status", "success");
    return response;
  }
}
