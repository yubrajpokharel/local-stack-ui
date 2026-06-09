package com.tools.localstackui.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
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
public class GcpService {

  private static final Logger LOGGER = LoggerFactory.getLogger(GcpService.class);

  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${gcp.project.id:localstack-ui}")
  private String projectId;

  @Value("${gcp.pubsub.host:localhost}")
  private String pubSubHost;

  @Value("${gcp.pubsub.port:8681}")
  private int pubSubPort;

  @Value("${gcp.pubsub.docker.service:gcp-pubsub}")
  private String pubSubDockerService;

  @Value("${gcp.pubsub.disable.credentials:true}")
  private boolean pubSubDisableCredentials;

  @Value("${gcp.pubsub.api.endpoint:}")
  private String pubSubApiEndpointOverride;

  @Value("${gcp.storage.endpoint:http://localhost:4443}")
  private String storageEndpoint;

  @Value("${gcp.storage.docker.service:gcp-storage}")
  private String storageDockerService;

  @Value("${gcp.firestore.host:localhost}")
  private String firestoreHost;

  @Value("${gcp.firestore.port:8787}")
  private int firestorePort;

  @Value("${gcp.firestore.endpoint:http://localhost:8787}")
  private String firestoreEndpoint;

  @Value("${gcp.firestore.docker.service:gcp-firestore}")
  private String firestoreDockerService;

  @Value("${gcp.firestore.auth.token:owner}")
  private String firestoreAuthToken;

  public Map<String, Object> getPubSubStatus() {
    return getSocketStatus("pubsub://" + pubSubHost + ":" + pubSubPort, pubSubHost, pubSubPort);
  }

  public Map<String, Object> getStorageStatus() {
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("uri", storageEndpoint);
    try {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(storageEndpoint + "/storage/v1/b?project=" + encode(projectId)))
          .timeout(Duration.ofMillis(1200))
          .GET()
          .build();
      HttpResponse<String> storageResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      response.put("running", storageResponse.statusCode() >= 200 && storageResponse.statusCode() < 500);
      response.put("status", storageResponse.statusCode() >= 200 && storageResponse.statusCode() < 500 ? "UP" : "DOWN");
    } catch (Exception e) {
      response.put("running", false);
      response.put("status", "DOWN");
      response.put("message", e.getMessage());
    }
    return response;
  }

  public Map<String, Object> getFirestoreStatus() {
    return getSocketStatus("firestore://" + firestoreHost + ":" + firestorePort, firestoreHost,
        firestorePort);
  }

  public String startPubSub() throws IOException, InterruptedException {
    return runDockerCompose("GCP Pub/Sub", "up", "-d", pubSubDockerService);
  }

  public String stopPubSub() throws IOException, InterruptedException {
    return runDockerCompose("GCP Pub/Sub", "stop", pubSubDockerService);
  }

  public List<Map<String, String>> getPubSubTopics() throws IOException, InterruptedException {
    List<Map<String, String>> topics = new ArrayList<>();
    String output = runPubSubCommand("gcloud", "pubsub", "topics", "list",
        "--project=" + projectId, "--format=value(name)");
    for (String line : output.split("\\R")) {
      String name = resourceName(line);
      if (!name.isEmpty()) {
        Map<String, String> topic = new LinkedHashMap<>();
        topic.put("name", name);
        topic.put("address", "projects/" + projectId + "/topics/" + name);
        topic.put("createdOn", "Unavailable");
        topics.add(topic);
      }
    }
    return topics;
  }

  public String createPubSubTopic(String topicName) throws IOException, InterruptedException {
    String safeTopicName = requireValue(topicName, "Topic name");
    String output = runPubSubCommand("gcloud", "pubsub", "topics", "create", safeTopicName,
        "--project=" + projectId);
    return output.isBlank() ? "Created topic " + safeTopicName : output;
  }

  public List<Map<String, String>> getPubSubSubscriptions() throws IOException, InterruptedException {
    List<Map<String, String>> subscriptions = new ArrayList<>();
    String output = runPubSubCommand("gcloud", "pubsub", "subscriptions", "list",
        "--project=" + projectId, "--format=value(name,topic)");
    for (String line : output.split("\\R")) {
      String[] parts = line.trim().split("\\s+");
      String name = parts.length > 0 ? resourceName(parts[0]) : "";
      if (!name.isEmpty()) {
        Map<String, String> subscription = new LinkedHashMap<>();
        subscription.put("name", name);
        subscription.put("topic", parts.length > 1 ? resourceName(parts[1]) : "");
        subscription.put("address", "projects/" + projectId + "/subscriptions/" + name);
        subscription.put("createdOn", "Unavailable");
        subscriptions.add(subscription);
      }
    }
    return subscriptions;
  }

  public Map<String, String> getPubSubSubscription(String subscriptionName)
      throws IOException, InterruptedException {
    String safeSubscriptionName = requireValue(subscriptionName, "Subscription name");
    for (Map<String, String> subscription : getPubSubSubscriptions()) {
      if (safeSubscriptionName.equals(subscription.get("name"))) {
        return subscription;
      }
    }
    Map<String, String> subscription = new LinkedHashMap<>();
    subscription.put("name", safeSubscriptionName);
    subscription.put("topic", "");
    subscription.put("address", "projects/" + projectId + "/subscriptions/" + safeSubscriptionName);
    subscription.put("createdOn", "Unavailable");
    return subscription;
  }

  public List<Map<String, String>> getPubSubSubscriptionsForTopic(String topicName)
      throws IOException, InterruptedException {
    String safeTopicName = requireValue(topicName, "Topic name");
    List<Map<String, String>> matchingSubscriptions = new ArrayList<>();
    for (Map<String, String> subscription : getPubSubSubscriptions()) {
      if (safeTopicName.equals(subscription.get("topic"))) {
        matchingSubscriptions.add(subscription);
      }
    }
    return matchingSubscriptions;
  }

  public String createPubSubSubscription(String subscriptionName, String topicName)
      throws IOException, InterruptedException {
    String safeSubscriptionName = requireValue(subscriptionName, "Subscription name");
    String safeTopicName = requireValue(topicName, "Topic name");
    String output = runPubSubCommand("gcloud", "pubsub", "subscriptions", "create",
        safeSubscriptionName, "--topic=" + safeTopicName, "--project=" + projectId);
    return output.isBlank() ? "Created subscription " + safeSubscriptionName : output;
  }

  public String publishPubSubMessage(String topicName, String message)
      throws IOException, InterruptedException {
    String safeTopicName = requireValue(topicName, "Topic name");
    String safeMessage = requireValue(message, "Message");
    String output = runPubSubCommand("gcloud", "pubsub", "topics", "publish", safeTopicName,
        "--message=" + safeMessage, "--project=" + projectId);
    return output.isBlank() ? "Published message to " + safeTopicName : output;
  }

  public List<Map<String, String>> pullPubSubMessages(String subscriptionName, int maxMessages)
      throws IOException, InterruptedException {
    String safeSubscriptionName = requireValue(subscriptionName, "Subscription name");
    int safeMaxMessages = Math.max(1, Math.min(maxMessages, 25));
    ObjectNode requestBody = objectMapper.createObjectNode();
    requestBody.put("maxMessages", safeMaxMessages);
    requestBody.put("returnImmediately", true);
    JsonNode response = requestJson("POST", pubSubApiEndpoint() + "v1/projects/" + projectId
        + "/subscriptions/" + encode(safeSubscriptionName) + ":pull",
        objectMapper.writeValueAsString(requestBody));
    List<Map<String, String>> messages = new ArrayList<>();
    JsonNode receivedMessages = response.path("receivedMessages");
    if (!receivedMessages.isArray()) {
      return messages;
    }
    int index = 1;
    for (JsonNode receivedMessage : receivedMessages) {
      JsonNode pubSubMessage = receivedMessage.path("message");
      Map<String, String> message = new LinkedHashMap<>();
      message.put("index", String.valueOf(index++));
      message.put("messageId", pubSubMessage.path("messageId").asText("Unavailable"));
      message.put("body", decodePubSubMessage(pubSubMessage.path("data").asText("")));
      message.put("publishTime", pubSubMessage.path("publishTime").asText("Unavailable"));
      message.put("ackId", receivedMessage.path("ackId").asText(""));
      message.put("orderingKey", pubSubMessage.path("orderingKey").asText(""));
      message.put("attributes", pubSubMessage.has("attributes")
          ? objectMapper.writeValueAsString(pubSubMessage.path("attributes")) : "{}");
      messages.add(message);
    }
    return messages;
  }

  public String startStorage() throws IOException, InterruptedException {
    return runDockerCompose("GCP Cloud Storage", "up", "-d", storageDockerService);
  }

  public String stopStorage() throws IOException, InterruptedException {
    return runDockerCompose("GCP Cloud Storage", "stop", storageDockerService);
  }

  public List<Map<String, String>> getStorageBuckets() throws IOException, InterruptedException {
    JsonNode root = requestJson("GET", storageEndpoint + "/storage/v1/b?project=" + encode(projectId), null);
    List<Map<String, String>> buckets = new ArrayList<>();
    JsonNode items = root.path("items");
    if (items.isArray()) {
      for (JsonNode item : items) {
        String name = item.path("name").asText();
        if (!name.isEmpty()) {
          Map<String, String> bucket = new LinkedHashMap<>();
          bucket.put("name", name);
          bucket.put("address", storageEndpoint + "/storage/v1/b/" + encode(name));
          bucket.put("createdOn", item.path("timeCreated").asText("Unavailable"));
          buckets.add(bucket);
        }
      }
    }
    return buckets;
  }

  public String createStorageBucket(String bucketName) throws IOException, InterruptedException {
    String safeBucketName = requireValue(bucketName, "Bucket name");
    String body = objectMapper.createObjectNode().put("name", safeBucketName).toString();
    requestJson("POST", storageEndpoint + "/storage/v1/b?project=" + encode(projectId), body);
    return "Created bucket " + safeBucketName;
  }

  public String deleteStorageBucket(String bucketName) throws IOException, InterruptedException {
    String safeBucketName = requireValue(bucketName, "Bucket name");
    requestJson("DELETE", storageEndpoint + "/storage/v1/b/" + encode(safeBucketName), null);
    return "Deleted bucket " + safeBucketName;
  }

  public List<Map<String, String>> getStorageObjects(String bucketName) throws IOException, InterruptedException {
    String safeBucketName = requireValue(bucketName, "Bucket name");
    JsonNode root = requestJson("GET", storageEndpoint + "/storage/v1/b/" + encode(safeBucketName) + "/o", null);
    List<Map<String, String>> objects = new ArrayList<>();
    JsonNode items = root.path("items");
    if (items.isArray()) {
      for (JsonNode item : items) {
        String name = item.path("name").asText();
        if (!name.isEmpty()) {
          Map<String, String> object = new LinkedHashMap<>();
          object.put("name", name);
          object.put("address", storageEndpoint + "/storage/v1/b/" + encode(safeBucketName)
              + "/o/" + encode(name));
          object.put("createdOn", item.path("timeCreated").asText("Unavailable"));
          objects.add(object);
        }
      }
    }
    return objects;
  }

  public String uploadStorageObject(String bucketName, String objectName, String content)
      throws IOException, InterruptedException {
    return uploadStorageObject(bucketName, objectName,
        (content == null ? "" : content).getBytes(StandardCharsets.UTF_8),
        "text/plain; charset=utf-8");
  }

  public String uploadStorageObject(String bucketName, String objectName, byte[] content,
      String contentType) throws IOException, InterruptedException {
    String safeBucketName = requireValue(bucketName, "Bucket name");
    String safeObjectName = requireValue(objectName, "Object name");
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(storageEndpoint + "/upload/storage/v1/b/" + encode(safeBucketName)
            + "/o?uploadType=media&name=" + encode(safeObjectName)))
        .timeout(Duration.ofSeconds(5))
        .header("Content-Type", contentType == null || contentType.isBlank()
            ? "application/octet-stream" : contentType)
        .POST(HttpRequest.BodyPublishers.ofByteArray(content == null ? new byte[0] : content))
        .build();
    sendRequest(request);
    return "Uploaded object " + safeObjectName;
  }

  public String deleteStorageObject(String bucketName, String objectName)
      throws IOException, InterruptedException {
    String safeBucketName = requireValue(bucketName, "Bucket name");
    String safeObjectName = requireValue(objectName, "Object name");
    requestJson("DELETE", storageEndpoint + "/storage/v1/b/" + encode(safeBucketName)
        + "/o/" + encode(safeObjectName), null);
    return "Deleted object " + safeObjectName;
  }

  public StorageObjectDownload downloadStorageObject(String bucketName, String objectName)
      throws IOException, InterruptedException {
    String safeBucketName = requireValue(bucketName, "Bucket name");
    String safeObjectName = requireValue(objectName, "Object name");
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(storageEndpoint + "/storage/v1/b/" + encode(safeBucketName)
            + "/o/" + encode(safeObjectName) + "?alt=media"))
        .timeout(Duration.ofSeconds(5))
        .GET()
        .build();
    HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      throw new IOException("HTTP " + response.statusCode() + ": "
          + new String(response.body(), StandardCharsets.UTF_8));
    }
    String contentType = response.headers().firstValue("Content-Type")
        .orElse("application/octet-stream");
    return new StorageObjectDownload(response.body(), contentType);
  }

  public String startFirestore() throws IOException, InterruptedException {
    return runDockerCompose("GCP Firestore", "up", "-d", firestoreDockerService);
  }

  public String stopFirestore() throws IOException, InterruptedException {
    return runDockerCompose("GCP Firestore", "stop", firestoreDockerService);
  }

  public List<Map<String, String>> getFirestoreCollections() throws IOException, InterruptedException {
    ObjectNode body = objectMapper.createObjectNode();
    body.put("pageSize", 100);
    JsonNode root = requestFirestoreJson("POST", firestoreDocumentsBaseUrl() + ":listCollectionIds",
        body.toString());
    List<Map<String, String>> collections = new ArrayList<>();
    JsonNode collectionIds = root.path("collectionIds");
    if (collectionIds.isArray()) {
      for (JsonNode collectionId : collectionIds) {
        String name = collectionId.asText();
        if (!name.isEmpty()) {
          Map<String, String> collection = new LinkedHashMap<>();
          collection.put("name", name);
          collection.put("address", firestoreDocumentsBasePath() + "/" + name);
          collection.put("createdOn", "Unavailable");
          collections.add(collection);
        }
      }
    }
    return collections;
  }

  public List<Map<String, String>> getFirestoreDocuments(String collectionName)
      throws IOException, InterruptedException {
    String safeCollectionName = requireValue(collectionName, "Collection name");
    JsonNode root = requestFirestoreJson("GET",
        firestoreDocumentsBaseUrl() + "/" + encode(safeCollectionName), null);
    List<Map<String, String>> documents = new ArrayList<>();
    JsonNode items = root.path("documents");
    if (items.isArray()) {
      for (JsonNode item : items) {
        String fullName = item.path("name").asText();
        String documentId = resourceName(fullName);
        if (!documentId.isEmpty()) {
          Map<String, String> document = new LinkedHashMap<>();
          document.put("name", documentId);
          document.put("address", fullName);
          document.put("createdOn", item.path("createTime").asText("Unavailable"));
          document.put("fields", firestoreFieldsToJson(item.path("fields")));
          documents.add(document);
        }
      }
    }
    return documents;
  }

  public String createFirestoreDocument(String collectionName, String documentName, String content)
      throws IOException, InterruptedException {
    String safeCollectionName = requireValue(collectionName, "Collection name");
    String safeDocumentName = requireValue(documentName, "Document name");
    ObjectNode body = objectMapper.createObjectNode();
    body.set("fields", toFirestoreFields(content));
    requestFirestoreJson("POST", firestoreDocumentsBaseUrl() + "/" + encode(safeCollectionName)
        + "?documentId=" + encode(safeDocumentName), body.toString());
    return "Created document " + safeCollectionName + "/" + safeDocumentName;
  }

  public String deleteFirestoreDocument(String collectionName, String documentName)
      throws IOException, InterruptedException {
    String safeCollectionName = requireValue(collectionName, "Collection name");
    String safeDocumentName = requireValue(documentName, "Document name");
    requestFirestoreJson("DELETE", firestoreDocumentsBaseUrl() + "/" + encode(safeCollectionName)
        + "/" + encode(safeDocumentName), null);
    return "Deleted document " + safeCollectionName + "/" + safeDocumentName;
  }

  private Map<String, Object> getSocketStatus(String uri, String host, int port) {
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("uri", uri);
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(host, port), 800);
      response.put("running", true);
      response.put("status", "UP");
    } catch (Exception e) {
      response.put("running", false);
      response.put("status", "DOWN");
      response.put("message", e.getMessage());
    }
    return response;
  }

  private String runPubSubCommand(String... command) throws IOException, InterruptedException {
    List<String> arguments = new ArrayList<>();
    arguments.add("exec");
    arguments.add("-T");
    arguments.add(pubSubDockerService);
    arguments.add("env");
    arguments.add("PUBSUB_EMULATOR_HOST=" + pubSubAddress());
    arguments.add("CLOUDSDK_API_ENDPOINT_OVERRIDES_PUBSUB=" + pubSubApiEndpoint());
    arguments.add("CLOUDSDK_CORE_PROJECT=" + projectId);
    arguments.add("CLOUDSDK_AUTH_DISABLE_CREDENTIALS=" + pubSubDisableCredentials);
    arguments.add("CLOUDSDK_CORE_DISABLE_PROMPTS=1");
    arguments.addAll(List.of(command));
    return runDockerCompose("GCP Pub/Sub", arguments.toArray(String[]::new));
  }

  private String pubSubAddress() {
    return pubSubHost + ":" + pubSubPort;
  }

  private String pubSubApiEndpoint() {
    String endpoint = pubSubApiEndpointOverride == null || pubSubApiEndpointOverride.isBlank()
        ? "http://" + pubSubAddress() + "/"
        : pubSubApiEndpointOverride.trim();
    return endpoint.endsWith("/") ? endpoint : endpoint + "/";
  }

  private String decodePubSubMessage(String data) {
    if (data == null || data.isBlank()) {
      return "";
    }
    try {
      return new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      return data;
    }
  }

  private String runDockerCompose(String serviceName, String... arguments)
      throws IOException, InterruptedException {
    List<String> command = new ArrayList<>();
    command.add("docker");
    command.add("compose");
    command.addAll(List.of(arguments));
    LOGGER.info("Running {} docker compose command: {}", serviceName, String.join(" ", command));
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();
    StringBuilder output = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        LOGGER.info("{} docker compose output: {}", serviceName, line);
        output.append(line).append("\n");
      }
    }
    int exitCode = process.waitFor();
    if (exitCode != 0) {
      LOGGER.error("{} docker compose command failed with exit code {}", serviceName, exitCode);
      throw new IOException(formatDockerComposeError(serviceName, exitCode, output.toString()));
    }
    LOGGER.info("{} docker compose command completed successfully with exit code {}", serviceName,
        exitCode);
    return output.toString().trim();
  }

  private JsonNode requestJson(String method, String url, String body)
      throws IOException, InterruptedException {
    return requestJson(method, url, body, null);
  }

  private JsonNode requestFirestoreJson(String method, String url, String body)
      throws IOException, InterruptedException {
    return requestJson(method, url, body, firestoreAuthToken);
  }

  private JsonNode requestJson(String method, String url, String body, String authToken)
      throws IOException, InterruptedException {
    HttpRequest.Builder builder = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .timeout(Duration.ofSeconds(5));
    if (authToken != null && !authToken.isBlank()) {
      builder.header("Authorization", "Bearer " + authToken.trim());
    }
    if ("POST".equals(method)) {
      builder.header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(body == null ? "{}" : body));
    } else if ("DELETE".equals(method)) {
      builder.DELETE();
    } else {
      builder.GET();
    }
    String response = sendRequest(builder.build());
    if (response == null || response.isBlank()) {
      return objectMapper.createObjectNode();
    }
    return objectMapper.readTree(response);
  }

  private String sendRequest(HttpRequest request) throws IOException, InterruptedException {
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
    return response.body();
  }

  private String formatDockerComposeError(String serviceName, int exitCode, String output) {
    if (output.contains("Cannot connect to the Docker daemon")) {
      return "Docker is not running. Start Docker Desktop and retry " + serviceName
          + ". Details: " + output.trim();
    }
    return "docker compose exited with code " + exitCode + ": " + output;
  }

  private String firestoreDocumentsBaseUrl() {
    return firestoreEndpoint + "/" + firestoreDocumentsBasePath();
  }

  private String firestoreDocumentsBasePath() {
    return "v1/projects/" + projectId + "/databases/(default)/documents";
  }

  private ObjectNode toFirestoreFields(String content) throws IOException {
    ObjectNode fields = objectMapper.createObjectNode();
    if (content == null || content.trim().isEmpty()) {
      fields.set("value", firestoreValue(""));
      return fields;
    }
    JsonNode json = objectMapper.readTree(content);
    if (json.isObject()) {
      json.fields().forEachRemaining(entry -> fields.set(entry.getKey(), firestoreValue(entry.getValue())));
    } else {
      fields.set("value", firestoreValue(json));
    }
    return fields;
  }

  private ObjectNode firestoreValue(JsonNode value) {
    ObjectNode wrapper = objectMapper.createObjectNode();
    if (value == null || value.isNull()) {
      wrapper.set("nullValue", objectMapper.getNodeFactory().textNode("NULL_VALUE"));
    } else if (value.isBoolean()) {
      wrapper.put("booleanValue", value.asBoolean());
    } else if (value.isInt() || value.isLong()) {
      wrapper.put("integerValue", value.asText());
    } else if (value.isFloat() || value.isDouble() || value.isBigDecimal()) {
      wrapper.put("doubleValue", value.asDouble());
    } else if (value.isArray()) {
      ObjectNode arrayValue = objectMapper.createObjectNode();
      ArrayNode values = objectMapper.createArrayNode();
      value.forEach(item -> values.add(firestoreValue(item)));
      arrayValue.set("values", values);
      wrapper.set("arrayValue", arrayValue);
    } else if (value.isObject()) {
      ObjectNode mapValue = objectMapper.createObjectNode();
      ObjectNode fields = objectMapper.createObjectNode();
      value.fields().forEachRemaining(entry -> fields.set(entry.getKey(), firestoreValue(entry.getValue())));
      mapValue.set("fields", fields);
      wrapper.set("mapValue", mapValue);
    } else {
      wrapper.put("stringValue", value.asText());
    }
    return wrapper;
  }

  private ObjectNode firestoreValue(String value) {
    ObjectNode wrapper = objectMapper.createObjectNode();
    wrapper.put("stringValue", value);
    return wrapper;
  }

  private String firestoreFieldsToJson(JsonNode fields) throws IOException {
    ObjectNode json = objectMapper.createObjectNode();
    if (fields != null && fields.isObject()) {
      fields.fields().forEachRemaining(entry -> json.set(entry.getKey(), fromFirestoreValue(entry.getValue())));
    }
    return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
  }

  private JsonNode fromFirestoreValue(JsonNode wrapper) {
    if (wrapper.has("nullValue")) {
      return objectMapper.nullNode();
    }
    if (wrapper.has("booleanValue")) {
      return objectMapper.getNodeFactory().booleanNode(wrapper.path("booleanValue").asBoolean());
    }
    if (wrapper.has("integerValue")) {
      return objectMapper.getNodeFactory().numberNode(wrapper.path("integerValue").asLong());
    }
    if (wrapper.has("doubleValue")) {
      return objectMapper.getNodeFactory().numberNode(wrapper.path("doubleValue").asDouble());
    }
    if (wrapper.has("arrayValue")) {
      ArrayNode array = objectMapper.createArrayNode();
      JsonNode values = wrapper.path("arrayValue").path("values");
      if (values.isArray()) {
        values.forEach(value -> array.add(fromFirestoreValue(value)));
      }
      return array;
    }
    if (wrapper.has("mapValue")) {
      ObjectNode map = objectMapper.createObjectNode();
      JsonNode fields = wrapper.path("mapValue").path("fields");
      if (fields.isObject()) {
        fields.fields().forEachRemaining(entry -> map.set(entry.getKey(), fromFirestoreValue(entry.getValue())));
      }
      return map;
    }
    return objectMapper.getNodeFactory().textNode(wrapper.path("stringValue").asText());
  }

  private String resourceName(String value) {
    if (value == null || value.trim().isEmpty()) {
      return "";
    }
    String[] parts = value.trim().split("/");
    return parts[parts.length - 1];
  }

  private String requireValue(String value, String label) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(label + " is required.");
    }
    return value.trim();
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  public record StorageObjectDownload(byte[] content, String contentType) {
  }
}
