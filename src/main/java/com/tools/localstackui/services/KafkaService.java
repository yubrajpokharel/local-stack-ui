package com.tools.localstackui.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KafkaService {

  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaService.class);

  @Value("${kafka.bootstrap.servers:localhost:9092}")
  private String bootstrapServers;

  @Value("${kafka.docker.service:kafka}")
  private String kafkaDockerService;

  @Value("${kafka.topic.command:/opt/bitnami/kafka/bin/kafka-topics.sh}")
  private String kafkaTopicCommand;

  @Value("${kafka.producer.command:/opt/kafka/bin/kafka-console-producer.sh}")
  private String kafkaProducerCommand;

  @Value("${kafka.offsets.command:/opt/kafka/bin/kafka-get-offsets.sh}")
  private String kafkaOffsetsCommand;

  public Map<String, Object> getStatus() {
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("uri", "kafka://" + bootstrapServers);
    response.put("bootstrapServers", bootstrapServers);
    try {
      HostAndPort hostAndPort = getFirstBootstrapServer();
      try (Socket socket = new Socket()) {
        socket.connect(new InetSocketAddress(hostAndPort.host(), hostAndPort.port()), 800);
      }
      response.put("running", true);
      response.put("status", "UP");
    } catch (Exception e) {
      response.put("running", false);
      response.put("status", "DOWN");
      response.put("message", e.getMessage());
    }
    return response;
  }

  public List<Map<String, String>> getTopicDetails() throws IOException, InterruptedException {
    List<Map<String, String>> topicDetails = new ArrayList<>();
    for (String topic : getTopics()) {
      Map<String, String> details = new LinkedHashMap<>();
      details.put("name", topic);
      details.put("address", bootstrapServers + "/" + topic);
      details.put("createdOn", "Unavailable");
      topicDetails.add(details);
    }
    return topicDetails;
  }

  public Map<String, String> getTopicDetail(String topicName)
      throws IOException, InterruptedException {
    String safeTopicName = requireValue(topicName, "Topic name");
    List<Map<String, String>> partitions = getTopicPartitions(safeTopicName);
    long messageCount = 0;
    for (Map<String, String> partition : partitions) {
      messageCount += Long.parseLong(partition.getOrDefault("messageCount", "0"));
    }
    Map<String, String> details = new LinkedHashMap<>();
    details.put("name", safeTopicName);
    details.put("address", bootstrapServers + "/" + safeTopicName);
    details.put("createdOn", "Unavailable");
    details.put("partitionCount", String.valueOf(partitions.size()));
    details.put("messageCount", String.valueOf(messageCount));
    details.put("replicationFactor", partitions.isEmpty()
        ? "Unavailable" : String.valueOf(partitions.get(0).getOrDefault("replicaCount", "Unavailable")));
    return details;
  }

  public List<String> getTopics() throws IOException, InterruptedException {
    String output = runDockerCompose("exec", "-T", kafkaDockerService, kafkaTopicCommand,
        "--bootstrap-server", "localhost:9092", "--list");
    List<String> topics = new ArrayList<>();
    for (String line : output.split("\\R")) {
      String topic = line.trim();
      if (!topic.isEmpty()) {
        topics.add(topic);
      }
    }
    topics.sort(String::compareToIgnoreCase);
    return topics;
  }

  public String createTopic(String topicName, int partitions, short replicationFactor)
      throws IOException, InterruptedException {
    String safeTopicName = requireValue(topicName, "Topic name");
    if (partitions < 1) {
      throw new IllegalArgumentException("Partitions must be at least 1.");
    }
    if (replicationFactor < 1) {
      throw new IllegalArgumentException("Replication factor must be at least 1.");
    }
    String output = runDockerCompose("exec", "-T", kafkaDockerService, kafkaTopicCommand,
        "--bootstrap-server", "localhost:9092", "--create", "--if-not-exists",
        "--topic", safeTopicName, "--partitions", String.valueOf(partitions),
        "--replication-factor", String.valueOf(replicationFactor));
    return output.isBlank() ? "Created topic " + safeTopicName : output;
  }

  public String publishMessage(String topicName, String message)
      throws IOException, InterruptedException {
    String safeTopicName = requireValue(topicName, "Topic name");
    String safeMessage = requireValue(message, "Message");
    String output = runDockerComposeWithInput(safeMessage + System.lineSeparator(), "exec", "-T",
        kafkaDockerService, kafkaProducerCommand, "--bootstrap-server", "localhost:9092",
        "--topic", safeTopicName);
    return output.isBlank() ? "Sent message to " + safeTopicName : output;
  }

  public List<Map<String, String>> getTopicPartitions(String topicName)
      throws IOException, InterruptedException {
    String safeTopicName = requireValue(topicName, "Topic name");
    Map<Integer, Map<String, String>> partitions = new LinkedHashMap<>();
    String output = runDockerCompose("exec", "-T", kafkaDockerService, kafkaTopicCommand,
        "--bootstrap-server", "localhost:9092", "--describe", "--topic", safeTopicName);
    for (String line : output.split("\\R")) {
      Map<String, String> values = parseKafkaKeyValueLine(line);
      String partitionId = values.get("Partition");
      if (partitionId != null && partitionId.matches("\\d+")) {
        Map<String, String> partition = new LinkedHashMap<>();
        partition.put("partition", partitionId);
        partition.put("leader", values.getOrDefault("Leader", "Unavailable"));
        partition.put("replicas", values.getOrDefault("Replicas", "Unavailable"));
        partition.put("isr", values.getOrDefault("Isr", "Unavailable"));
        partition.put("replicaCount", String.valueOf(countCsv(values.get("Replicas"))));
        partitions.put(Integer.parseInt(partitionId), partition);
      }
    }
    addOffsets(partitions, safeTopicName, "-2", "beginningOffset");
    addOffsets(partitions, safeTopicName, "-1", "endOffset");
    for (Map<String, String> partition : partitions.values()) {
      long beginningOffset = parseLong(partition.get("beginningOffset"));
      long endOffset = parseLong(partition.get("endOffset"));
      partition.put("messageCount", String.valueOf(Math.max(0, endOffset - beginningOffset)));
    }
    return new ArrayList<>(partitions.values());
  }

  public String startKafka() throws IOException, InterruptedException {
    return runDockerCompose("up", "-d", kafkaDockerService);
  }

  public String stopKafka() throws IOException, InterruptedException {
    return runDockerCompose("stop", kafkaDockerService);
  }

  private String runDockerCompose(String... arguments) throws IOException, InterruptedException {
    List<String> command = new ArrayList<>();
    command.add("docker");
    command.add("compose");
    command.addAll(List.of(arguments));
    LOGGER.info("Running Kafka docker compose command: {}", String.join(" ", command));
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();
    StringBuilder output = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        LOGGER.info("Kafka docker compose output: {}", line);
        output.append(line).append("\n");
      }
    }
    int exitCode = process.waitFor();
    if (exitCode != 0) {
      LOGGER.error("Kafka docker compose command failed with exit code {}", exitCode);
      throw new IOException(formatDockerComposeError(exitCode, output.toString()));
    }
    LOGGER.info("Kafka docker compose command completed successfully with exit code {}", exitCode);
    return output.toString().trim();
  }

  private String runDockerComposeWithInput(String input, String... arguments)
      throws IOException, InterruptedException {
    List<String> command = new ArrayList<>();
    command.add("docker");
    command.add("compose");
    command.addAll(List.of(arguments));
    LOGGER.info("Running Kafka docker compose command: {}", String.join(" ", command));
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();
    process.getOutputStream().write(input.getBytes(StandardCharsets.UTF_8));
    process.getOutputStream().close();
    StringBuilder output = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        LOGGER.info("Kafka docker compose output: {}", line);
        output.append(line).append("\n");
      }
    }
    int exitCode = process.waitFor();
    if (exitCode != 0) {
      LOGGER.error("Kafka docker compose command failed with exit code {}", exitCode);
      throw new IOException(formatDockerComposeError(exitCode, output.toString()));
    }
    LOGGER.info("Kafka docker compose command completed successfully with exit code {}", exitCode);
    return output.toString().trim();
  }

  private void addOffsets(Map<Integer, Map<String, String>> partitions, String topicName,
      String time, String fieldName) throws IOException, InterruptedException {
    String output = runDockerCompose("exec", "-T", kafkaDockerService, kafkaOffsetsCommand,
        "--bootstrap-server", "localhost:9092", "--topic", topicName, "--time", time);
    for (String line : output.split("\\R")) {
      String[] parts = line.trim().split(":");
      if (parts.length < 3 || !parts[1].matches("\\d+")) {
        continue;
      }
      Integer partitionId = Integer.parseInt(parts[1]);
      Map<String, String> partition = partitions.computeIfAbsent(partitionId, id -> {
        Map<String, String> value = new LinkedHashMap<>();
        value.put("partition", String.valueOf(id));
        value.put("leader", "Unavailable");
        value.put("replicas", "Unavailable");
        value.put("isr", "Unavailable");
        value.put("replicaCount", "Unavailable");
        return value;
      });
      partition.put(fieldName, parts[2]);
    }
  }

  private Map<String, String> parseKafkaKeyValueLine(String line) {
    Map<String, String> values = new LinkedHashMap<>();
    String[] tokens = line.trim().split("\\s+");
    for (int i = 0; i < tokens.length - 1; i++) {
      if (tokens[i].endsWith(":")) {
        values.put(tokens[i].substring(0, tokens[i].length() - 1), tokens[i + 1]);
        i++;
      }
    }
    return values;
  }

  private int countCsv(String value) {
    if (value == null || value.isBlank() || "Unavailable".equals(value)) {
      return 0;
    }
    return value.split(",").length;
  }

  private long parseLong(String value) {
    try {
      return Long.parseLong(value);
    } catch (Exception e) {
      return 0;
    }
  }

  private String formatDockerComposeError(int exitCode, String output) {
    if (output.contains("Cannot connect to the Docker daemon")) {
      return "Docker is not running. Start Docker Desktop and retry Kafka. Details: "
          + output.trim();
    }
    return "docker compose exited with code " + exitCode + ": " + output;
  }

  private HostAndPort getFirstBootstrapServer() {
    String firstServer = bootstrapServers.split(",")[0].trim();
    String[] parts = firstServer.split(":");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid Kafka bootstrap server: " + firstServer);
    }
    return new HostAndPort(parts[0], Integer.parseInt(parts[1]));
  }

  private String requireValue(String value, String label) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(label + " is required.");
    }
    return value.trim();
  }

  private record HostAndPort(String host, int port) {
  }
}
