package com.tools.localstackui.services;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

  @Autowired
  StringRedisTemplate redisTemplate;

  @Value("${spring.data.redis.host:localhost}")
  private String redisHost;

  @Value("${spring.data.redis.port:6379}")
  private int redisPort;

  public String ping() {
    return redisTemplate.execute((RedisCallback<String>) connection -> connection.ping());
  }

  public Map<String, Object> getStatus() {
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("uri", "redis://" + redisHost + ":" + redisPort);
    try {
      response.put("response", ping());
      response.put("running", true);
      response.put("status", "UP");
    } catch (Exception e) {
      response.put("running", false);
      response.put("status", "DOWN");
      response.put("message", e.getMessage());
    }
    return response;
  }

  public List<String> getKeys() {
    Set<String> keys = redisTemplate.keys("*");
    if (keys == null || keys.isEmpty()) {
      return emptyList();
    }
    return keys.stream().sorted().collect(toList());
  }

  public List<Map<String, String>> getKeyDetails() {
    return getKeys().stream().map(key -> {
      Map<String, String> keyDetails = new LinkedHashMap<>();
      keyDetails.put("name", key);
      keyDetails.put("address", "redis://" + redisHost + ":" + redisPort + "/" + key);
      keyDetails.put("createdOn", "");
      return keyDetails;
    }).toList();
  }

  public String getValue(String key) {
    return redisTemplate.opsForValue().get(key);
  }

  public void setValue(String key, String value) {
    redisTemplate.opsForValue().set(key, value);
  }

  public Boolean delete(String key) {
    return redisTemplate.delete(key);
  }

  public String startRedis() throws IOException, InterruptedException {
    return runDockerCompose("up", "-d", "redis");
  }

  public String stopRedis() throws IOException, InterruptedException {
    return runDockerCompose("stop", "redis");
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
      return "Docker is not running. Start Docker Desktop and retry Redis. Details: "
          + output.trim();
    }
    return "docker compose exited with code " + exitCode + ": " + output;
  }
}
