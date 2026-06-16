package com.tools.localstackui.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMqService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqService.class);

  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${rabbitmq.host:localhost}")
  private String rabbitHost;

  @Value("${rabbitmq.port:5672}")
  private int rabbitPort;

  @Value("${rabbitmq.management.endpoint:http://localhost:15672}")
  private String managementEndpoint;

  @Value("${rabbitmq.username:guest}")
  private String username;

  @Value("${rabbitmq.password:guest}")
  private String password;

  @Value("${rabbitmq.vhost:/}")
  private String vhost;

  @Value("${rabbitmq.docker.service:rabbitmq}")
  private String rabbitDockerService;

  public Map<String, Object> getStatus() {
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("uri", "amqp://" + rabbitHost + ":" + rabbitPort + "/" + encodePath(vhost));
    response.put("managementUri", managementEndpoint);
    try {
      JsonNode overview = requestJson("GET", "/api/overview", null);
      response.put("running", true);
      response.put("status", "UP");
      response.put("productName", overview.path("product_name").asText("RabbitMQ"));
      response.put("version", overview.path("rabbitmq_version").asText("Unavailable"));
    } catch (Exception e) {
      response.put("running", false);
      response.put("status", "DOWN");
      response.put("message", e.getMessage());
    }
    return response;
  }

  public String startRabbitMq() throws IOException, InterruptedException {
    return runDockerCompose("up", "-d", rabbitDockerService);
  }

  public String stopRabbitMq() throws IOException, InterruptedException {
    return runDockerCompose("stop", rabbitDockerService);
  }

  public List<Map<String, String>> getQueues() throws IOException, InterruptedException {
    JsonNode queues = requestJson("GET", "/api/queues/" + encodePath(vhost), null);
    List<Map<String, String>> result = new ArrayList<>();
    for (JsonNode queue : queues) {
      String name = queue.path("name").asText();
      Map<String, String> details = new LinkedHashMap<>();
      details.put("name", name);
      details.put("address", "amqp://" + rabbitHost + ":" + rabbitPort + "/"
          + encodePath(vhost) + "/" + name);
      details.put("createdOn", "Unavailable");
      details.put("messages", queue.path("messages").asText("0"));
      details.put("ready", queue.path("messages_ready").asText("0"));
      details.put("unacked", queue.path("messages_unacknowledged").asText("0"));
      details.put("consumers", queue.path("consumers").asText("0"));
      details.put("durable", queue.path("durable").asText("false"));
      details.put("autoDelete", queue.path("auto_delete").asText("false"));
      details.put("type", queue.path("type").asText("classic"));
      result.add(details);
    }
    result.sort((first, second) -> first.get("name").compareToIgnoreCase(second.get("name")));
    return result;
  }

  public String createQueue(String queueName, boolean durable, boolean autoDelete, String queueType)
      throws IOException, InterruptedException {
    String safeQueueName = requireValue(queueName, "Queue name");
    String safeQueueType = requireQueueType(queueType);
    ObjectNode body = objectMapper.createObjectNode();
    body.put("durable", durable);
    body.put("auto_delete", autoDelete);
    ObjectNode arguments = objectMapper.createObjectNode();
    if (!"classic".equals(safeQueueType)) {
      arguments.put("x-queue-type", safeQueueType);
    }
    body.set("arguments", arguments);
    requestJson("PUT", "/api/queues/" + encodePath(vhost) + "/" + encodePath(safeQueueName),
        body.toString());
    return "Created queue " + safeQueueName;
  }

  public String deleteQueue(String queueName) throws IOException, InterruptedException {
    String safeQueueName = requireValue(queueName, "Queue name");
    requestJson("DELETE", "/api/queues/" + encodePath(vhost) + "/" + encodePath(safeQueueName),
        null);
    return "Deleted queue " + safeQueueName;
  }

  public String publishMessage(String queueName, String message)
      throws IOException, InterruptedException {
    String safeQueueName = requireValue(queueName, "Queue name");
    String safeMessage = requireValue(message, "Message");
    ObjectNode body = objectMapper.createObjectNode();
    body.set("properties", objectMapper.createObjectNode());
    body.put("routing_key", safeQueueName);
    body.put("payload", safeMessage);
    body.put("payload_encoding", "string");
    JsonNode response = requestJson("POST",
        "/api/exchanges/" + encodePath(vhost) + "/amq.default/publish", body.toString());
    if (!response.path("routed").asBoolean(false)) {
      throw new IOException("RabbitMQ did not route the message to " + safeQueueName + ".");
    }
    return "Sent message to " + safeQueueName;
  }

  public List<Map<String, String>> peekMessages(String queueName, int count)
      throws IOException, InterruptedException {
    String safeQueueName = requireValue(queueName, "Queue name");
    int safeCount = Math.max(1, Math.min(count, 50));
    ObjectNode body = objectMapper.createObjectNode();
    body.put("count", safeCount);
    body.put("ackmode", "ack_requeue_true");
    body.put("encoding", "auto");
    body.put("truncate", 50000);
    JsonNode messages = requestJson("POST",
        "/api/queues/" + encodePath(vhost) + "/" + encodePath(safeQueueName) + "/get",
        body.toString());
    List<Map<String, String>> result = new ArrayList<>();
    for (JsonNode message : messages) {
      Map<String, String> item = new LinkedHashMap<>();
      item.put("payload", message.path("payload").asText(""));
      item.put("payloadBytes", message.path("payload_bytes").asText("0"));
      item.put("exchange", message.path("exchange").asText(""));
      item.put("routingKey", message.path("routing_key").asText(""));
      item.put("redelivered", message.path("redelivered").asText("false"));
      result.add(item);
    }
    return result;
  }

  private JsonNode requestJson(String method, String path, String body)
      throws IOException, InterruptedException {
    HttpRequest.Builder builder = HttpRequest.newBuilder()
        .uri(URI.create(managementEndpoint + path))
        .timeout(Duration.ofSeconds(3))
        .header("Authorization", basicAuthHeader())
        .header("Content-Type", "application/json");
    if ("POST".equals(method) || "PUT".equals(method)) {
      builder.method(method, HttpRequest.BodyPublishers.ofString(body == null ? "{}" : body));
    } else if ("DELETE".equals(method)) {
      builder.DELETE();
    } else {
      builder.GET();
    }
    HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      throw new IOException("RabbitMQ management API returned HTTP " + response.statusCode()
          + ": " + response.body());
    }
    if (response.body() == null || response.body().isBlank()) {
      return objectMapper.createObjectNode();
    }
    return objectMapper.readTree(response.body());
  }

  private String runDockerCompose(String... arguments) throws IOException, InterruptedException {
    List<String> command = new ArrayList<>();
    command.add("docker");
    command.add("compose");
    command.addAll(List.of(arguments));
    LOGGER.info("Running RabbitMQ docker compose command: {}", String.join(" ", command));
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();
    StringBuilder output = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        LOGGER.info("RabbitMQ docker compose output: {}", line);
        output.append(line).append("\n");
      }
    }
    int exitCode = process.waitFor();
    if (exitCode != 0) {
      LOGGER.error("RabbitMQ docker compose command failed with exit code {}", exitCode);
      throw new IOException(formatDockerComposeError(exitCode, output.toString()));
    }
    return output.toString().trim();
  }

  private String basicAuthHeader() {
    String value = username + ":" + password;
    return "Basic " + Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
  }

  private String encodePath(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
  }

  private String requireQueueType(String queueType) {
    String value = queueType == null || queueType.isBlank() ? "classic" : queueType.trim();
    if (!List.of("classic", "quorum").contains(value)) {
      throw new IllegalArgumentException("Queue type must be classic or quorum.");
    }
    return value;
  }

  private String requireValue(String value, String label) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(label + " is required.");
    }
    return value.trim();
  }

  private String formatDockerComposeError(int exitCode, String output) {
    if (output.contains("Cannot connect to the Docker daemon")) {
      return "Docker is not running. Start Docker Desktop and retry RabbitMQ. Details: "
          + output.trim();
    }
    return "docker compose exited with code " + exitCode + ": " + output;
  }
}
