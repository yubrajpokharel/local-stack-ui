package com.tools.localstackui.services;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class MongoDbService {

  @Autowired
  MongoClient mongoClient;

  @Autowired
  MongoTemplate mongoTemplate;

  @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/localstackui}")
  private String mongoUri;

  public Map<String, Object> getStatus() {
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("uri", mongoUri);
    try {
      Document ping = mongoTemplate.executeCommand("{ ping: 1 }");
      response.put("running", true);
      response.put("status", "UP");
      response.put("response", ping.toJson());
    } catch (Exception e) {
      response.put("running", false);
      response.put("status", "DOWN");
      response.put("message", e.getMessage());
    }
    return response;
  }

  public List<String> getDatabases() {
    List<String> databases = new ArrayList<>();
    mongoClient.listDatabaseNames().forEach(databases::add);
    return databases;
  }

  public List<String> getCollections(String databaseName) {
    String safeDatabaseName = requireValue(databaseName, "Database name");
    List<String> collections = new ArrayList<>();
    mongoClient.getDatabase(safeDatabaseName).listCollectionNames().forEach(collections::add);
    return collections;
  }

  public String createDatabase(String databaseName, String collectionName) {
    String safeDatabaseName = requireValue(databaseName, "Database name");
    String safeCollectionName = collectionName == null || collectionName.trim().isEmpty()
        ? "default_collection" : collectionName.trim();
    createCollection(safeDatabaseName, safeCollectionName);
    return "Created " + safeDatabaseName + "." + safeCollectionName;
  }

  public String createCollection(String databaseName, String collectionName) {
    String safeDatabaseName = requireValue(databaseName, "Database name");
    String safeCollectionName = requireValue(collectionName, "Collection name");
    MongoDatabase database = mongoClient.getDatabase(safeDatabaseName);
    if (!getCollections(safeDatabaseName).contains(safeCollectionName)) {
      database.createCollection(safeCollectionName);
    }
    return "Created collection " + safeDatabaseName + "." + safeCollectionName;
  }

  public String startMongoDb() throws IOException, InterruptedException {
    return runDockerCompose("up", "-d", "mongodb");
  }

  public String stopMongoDb() throws IOException, InterruptedException {
    return runDockerCompose("stop", "mongodb");
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
      return "Docker is not running. Start Docker Desktop and retry MongoDB. Details: "
          + output.trim();
    }
    return "docker compose exited with code " + exitCode + ": " + output;
  }

  private String requireValue(String value, String label) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(label + " is required.");
    }
    return value.trim();
  }
}
