package com.tools.localstackui.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GcpSecretManagerService {

  private static final DateTimeFormatter UI_TIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${gcp.project.id:localstack-ui}")
  private String projectId;

  @Value("${gcp.secretmanager.data.file:./volume/gcp-secret-manager/secrets.json}")
  private String dataFile;

  @Value("${server.port:8085}")
  private int serverPort;

  public Map<String, Object> getStatus() {
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("uri", "http://localhost:" + serverPort + "/gcp/secret-manager");
    response.put("apiUri", "http://localhost:" + serverPort + "/gcp/secret-manager");
    response.put("resourceName", "projects/" + projectId);
    response.put("running", true);
    response.put("status", "UP");
    return response;
  }

  public synchronized List<Map<String, String>> getSecretDetails() throws IOException {
    ObjectNode root = readStore();
    List<Map<String, String>> secrets = new ArrayList<>();
    JsonNode secretStore = root.path("secrets");
    if (!secretStore.isObject()) {
      return secrets;
    }
    secretStore.fields().forEachRemaining(entry -> {
      JsonNode secret = entry.getValue();
      Map<String, String> details = new LinkedHashMap<>();
      details.put("name", secret.path("name").asText(entry.getKey()));
      details.put("resourceName", secret.path("resourceName").asText(resourceName(entry.getKey())));
      details.put("address", secret.path("resourceName").asText(resourceName(entry.getKey())));
      details.put("createdOn", secret.path("createdOn").asText("Unavailable"));
      details.put("lastChangedOn", secret.path("updatedOn").asText("Unavailable"));
      details.put("latestVersion", String.valueOf(latestVersion(secret)));
      details.put("versionCount", String.valueOf(versionCount(secret)));
      secrets.add(details);
    });
    secrets.sort(Comparator.comparing(secret -> secret.get("name"), String.CASE_INSENSITIVE_ORDER));
    return secrets;
  }

  public synchronized Map<String, String> getSecretValue(String secretId) throws IOException {
    String safeSecretId = requireSecretId(secretId);
    JsonNode secret = requireSecret(readStore(), safeSecretId);
    JsonNode latest = latestVersionNode(secret);
    Map<String, String> response = new LinkedHashMap<>();
    response.put("name", safeSecretId);
    response.put("resourceName", secret.path("resourceName").asText(resourceName(safeSecretId)));
    response.put("version", latest.path("version").asText("0"));
    response.put("versionResourceName", versionResourceName(safeSecretId, latest.path("version").asText("0")));
    response.put("value", latest.path("value").asText(""));
    response.put("createdOn", latest.path("createdOn").asText("Unavailable"));
    return response;
  }

  public synchronized Map<String, String> createSecret(String secretId, String secretValue)
      throws IOException {
    String safeSecretId = requireSecretId(secretId);
    String safeValue = requireValue(secretValue, "Secret value");
    ObjectNode root = readStore();
    ObjectNode secrets = secretsNode(root);
    if (secrets.has(safeSecretId)) {
      throw new IllegalArgumentException("Secret already exists: " + safeSecretId);
    }
    String now = now();
    ObjectNode secret = objectMapper.createObjectNode();
    secret.put("name", safeSecretId);
    secret.put("resourceName", resourceName(safeSecretId));
    secret.put("replication", "automatic");
    secret.put("createdOn", now);
    secret.put("updatedOn", now);
    ArrayNode versions = objectMapper.createArrayNode();
    versions.add(versionNode("1", safeValue, now));
    secret.set("versions", versions);
    secrets.set(safeSecretId, secret);
    writeStore(root);
    return response("Created secret " + safeSecretId, safeSecretId, "1");
  }

  public synchronized Map<String, String> updateSecretValue(String secretId, String secretValue)
      throws IOException {
    String safeSecretId = requireSecretId(secretId);
    String safeValue = requireValue(secretValue, "Secret value");
    ObjectNode root = readStore();
    ObjectNode secret = (ObjectNode) requireSecret(root, safeSecretId);
    int nextVersion = latestVersion(secret) + 1;
    String now = now();
    ArrayNode versions = versionsNode(secret);
    versions.add(versionNode(String.valueOf(nextVersion), safeValue, now));
    secret.put("updatedOn", now);
    writeStore(root);
    return response("Added version " + nextVersion + " to " + safeSecretId, safeSecretId,
        String.valueOf(nextVersion));
  }

  public synchronized String deleteSecret(String secretId) throws IOException {
    String safeSecretId = requireSecretId(secretId);
    ObjectNode root = readStore();
    ObjectNode secrets = secretsNode(root);
    if (!secrets.has(safeSecretId)) {
      throw new IllegalArgumentException("Secret not found: " + safeSecretId);
    }
    secrets.remove(safeSecretId);
    writeStore(root);
    return "Deleted secret " + safeSecretId;
  }

  private ObjectNode readStore() throws IOException {
    Path path = dataPath();
    if (!Files.exists(path)) {
      ObjectNode root = objectMapper.createObjectNode();
      root.set("secrets", objectMapper.createObjectNode());
      return root;
    }
    JsonNode root = objectMapper.readTree(path.toFile());
    if (root != null && root.isObject()) {
      ObjectNode object = (ObjectNode) root;
      secretsNode(object);
      return object;
    }
    ObjectNode object = objectMapper.createObjectNode();
    object.set("secrets", objectMapper.createObjectNode());
    return object;
  }

  private void writeStore(ObjectNode root) throws IOException {
    Path path = dataPath();
    Files.createDirectories(path.getParent());
    objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), root);
  }

  private Path dataPath() {
    return Path.of(dataFile).toAbsolutePath().normalize();
  }

  private ObjectNode secretsNode(ObjectNode root) {
    if (!root.has("secrets") || !root.path("secrets").isObject()) {
      root.set("secrets", objectMapper.createObjectNode());
    }
    return (ObjectNode) root.path("secrets");
  }

  private ArrayNode versionsNode(ObjectNode secret) {
    if (!secret.has("versions") || !secret.path("versions").isArray()) {
      secret.set("versions", objectMapper.createArrayNode());
    }
    return (ArrayNode) secret.path("versions");
  }

  private JsonNode requireSecret(ObjectNode root, String secretId) {
    JsonNode secret = secretsNode(root).path(secretId);
    if (secret.isMissingNode() || !secret.isObject()) {
      throw new IllegalArgumentException("Secret not found: " + secretId);
    }
    return secret;
  }

  private ObjectNode versionNode(String version, String value, String createdOn) {
    ObjectNode node = objectMapper.createObjectNode();
    node.put("version", version);
    node.put("state", "ENABLED");
    node.put("createdOn", createdOn);
    node.put("value", value);
    return node;
  }

  private JsonNode latestVersionNode(JsonNode secret) {
    JsonNode versions = secret.path("versions");
    if (!versions.isArray() || versions.isEmpty()) {
      throw new IllegalArgumentException("Secret has no versions: " + secret.path("name").asText());
    }
    return versions.get(versions.size() - 1);
  }

  private int latestVersion(JsonNode secret) {
    JsonNode latest = latestVersionNodeOrNull(secret);
    if (latest == null) {
      return 0;
    }
    return parseInt(latest.path("version").asText("0"));
  }

  private JsonNode latestVersionNodeOrNull(JsonNode secret) {
    JsonNode versions = secret.path("versions");
    if (!versions.isArray() || versions.isEmpty()) {
      return null;
    }
    return versions.get(versions.size() - 1);
  }

  private int versionCount(JsonNode secret) {
    JsonNode versions = secret.path("versions");
    return versions.isArray() ? versions.size() : 0;
  }

  private Map<String, String> response(String message, String secretId, String version) {
    Map<String, String> response = new LinkedHashMap<>();
    response.put("message", message);
    response.put("name", secretId);
    response.put("resourceName", resourceName(secretId));
    response.put("version", version);
    response.put("versionResourceName", versionResourceName(secretId, version));
    return response;
  }

  private String resourceName(String secretId) {
    return "projects/" + projectId + "/secrets/" + secretId;
  }

  private String versionResourceName(String secretId, String version) {
    return resourceName(secretId) + "/versions/" + version;
  }

  private String requireSecretId(String value) {
    String secretId = requireValue(value, "Secret ID");
    if (!secretId.matches("[A-Za-z0-9_-]{1,255}")) {
      throw new IllegalArgumentException("Secret ID must contain only letters, numbers, underscores, and hyphens.");
    }
    return secretId;
  }

  private String requireValue(String value, String label) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(label + " is required.");
    }
    return value.trim();
  }

  private int parseInt(String value) {
    try {
      return Integer.parseInt(value);
    } catch (Exception e) {
      return 0;
    }
  }

  private String now() {
    return LocalDateTime.now().format(UI_TIME_FORMAT);
  }
}
