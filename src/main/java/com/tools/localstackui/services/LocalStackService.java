package com.tools.localstackui.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LocalStackService {

  @Value("${localstack.endpoint:http://localhost:4566}")
  private String localStackEndpoint;

  @Value("${localstack.docker.service:localstack}")
  private String localStackDockerService;

  public Map<String, Object> getStatus() {
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("uri", localStackEndpoint);
    try {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(localStackEndpoint + "/_localstack/health"))
          .timeout(Duration.ofMillis(1200))
          .GET()
          .build();
      HttpResponse<String> health = HttpClient.newHttpClient()
          .send(request, HttpResponse.BodyHandlers.ofString());
      response.put("running", health.statusCode() >= 200 && health.statusCode() < 500);
      response.put("status", health.statusCode() >= 200 && health.statusCode() < 500 ? "UP" : "DOWN");
      response.put("response", health.body());
    } catch (Exception e) {
      response.put("running", false);
      response.put("status", "DOWN");
      response.put("message", e.getMessage());
    }
    return response;
  }

  public String startLocalStack() throws IOException, InterruptedException {
    return runDockerCompose("up", "-d", localStackDockerService);
  }

  public String stopLocalStack() throws IOException, InterruptedException {
    return runDockerCompose("stop", localStackDockerService);
  }

  private String runDockerCompose(String... arguments) throws IOException, InterruptedException {
    List<String> command = new ArrayList<>();
    command.add("docker");
    command.add("compose");
    command.addAll(List.of(arguments));
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();
    StringBuilder output = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
      }
    }
    int exitCode = process.waitFor();
    if (exitCode != 0) {
      throw new IOException(formatDockerComposeError(exitCode, output.toString()));
    }
    return output.toString().trim();
  }

  private String formatDockerComposeError(int exitCode, String output) {
    if (output.contains("Cannot connect to the Docker daemon")) {
      return "Docker is not running. Start Docker Desktop and retry LocalStack. Details: "
          + output.trim();
    }
    return "docker compose exited with code " + exitCode + ": " + output;
  }
}
