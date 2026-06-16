package com.tools.localstackui.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class KubernetesService {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public Map<String, Object> getStatus(String provider) {
    Map<String, Object> status = new LinkedHashMap<>();
    status.put("provider", provider);
    try {
      String context = runCommand("kubectl", "config", "current-context");
      String version = getKubectlClientVersion();
      status.put("running", true);
      status.put("status", "UP");
      status.put("context", context.trim());
      status.put("clientVersion", version.trim());
      status.put("uri", "kubernetes://" + context.trim());
    } catch (Exception e) {
      status.put("running", false);
      status.put("status", "DOWN");
      status.put("context", "Unavailable");
      status.put("uri", "kubernetes://unavailable");
      status.put("message", e.getMessage());
    }
    return status;
  }

  private String getKubectlClientVersion() throws IOException, InterruptedException {
    String output = runCommand("kubectl", "version", "--client", "-o", "json");
    try {
      JsonNode root = objectMapper.readTree(output);
      JsonNode clientVersion = root.path("clientVersion");
      String gitVersion = clientVersion.path("gitVersion").asText("");
      return gitVersion.isBlank() ? output.trim() : gitVersion;
    } catch (Exception e) {
      return output.trim();
    }
  }

  public List<Map<String, String>> getNamespaces() throws IOException, InterruptedException {
    JsonNode root = runKubectlJson("get", "namespaces", "-o", "json");
    List<Map<String, String>> namespaces = new ArrayList<>();
    for (JsonNode item : root.path("items")) {
      Map<String, String> namespace = new LinkedHashMap<>();
      namespace.put("name", item.path("metadata").path("name").asText());
      namespace.put("status", item.path("status").path("phase").asText("Unavailable"));
      namespace.put("createdOn", formatAge(item.path("metadata").path("creationTimestamp").asText("")));
      namespaces.add(namespace);
    }
    return namespaces;
  }

  public Map<String, Object> getNamespaceOverview(String namespace)
      throws IOException, InterruptedException {
    String safeNamespace = requireValue(namespace, "Namespace");
    Map<String, Object> overview = new LinkedHashMap<>();
    overview.put("pods", getPods(safeNamespace));
    overview.put("deployments", getDeployments(safeNamespace));
    overview.put("services", getServices(safeNamespace));
    return overview;
  }

  public String getEvents(String namespace) throws IOException, InterruptedException {
    return runCommand("kubectl", "--request-timeout=5s", "get", "events", "-n",
        requireValue(namespace, "Namespace"), "--sort-by=.lastTimestamp");
  }

  public String getLogs(String namespace, String pod, String container)
      throws IOException, InterruptedException {
    List<String> command = new ArrayList<>();
    command.add("kubectl");
    command.add("--request-timeout=5s");
    command.add("logs");
    command.add("-n");
    command.add(requireValue(namespace, "Namespace"));
    command.add(requireValue(pod, "Pod"));
    if (container != null && !container.isBlank()) {
      command.add("-c");
      command.add(container.trim());
    }
    command.add("--tail=200");
    return runCommand(command.toArray(String[]::new));
  }

  public String createService(String namespace, String deploymentName, String serviceName,
      String serviceType, int port, int targetPort) throws IOException, InterruptedException {
    String safeNamespace = requireValue(namespace, "Namespace");
    String safeDeploymentName = requireValue(deploymentName, "Deployment");
    String safeServiceName = requireValue(serviceName, "Service name");
    String safeServiceType = requireServiceType(serviceType);
    if (port < 1 || port > 65535) {
      throw new IllegalArgumentException("Port must be between 1 and 65535.");
    }
    if (targetPort < 1 || targetPort > 65535) {
      throw new IllegalArgumentException("Target port must be between 1 and 65535.");
    }
    return runCommand("kubectl", "--request-timeout=5s", "expose", "deployment",
        safeDeploymentName, "-n", safeNamespace, "--name", safeServiceName, "--type",
        safeServiceType, "--port", String.valueOf(port), "--target-port",
        String.valueOf(targetPort));
  }

  private List<Map<String, String>> getPods(String namespace)
      throws IOException, InterruptedException {
    JsonNode root = runKubectlJson("get", "pods", "-n", namespace, "-o", "json");
    List<Map<String, String>> pods = new ArrayList<>();
    for (JsonNode item : root.path("items")) {
      JsonNode metadata = item.path("metadata");
      JsonNode status = item.path("status");
      int restarts = 0;
      int ready = 0;
      int total = 0;
      List<String> containers = new ArrayList<>();
      for (JsonNode containerStatus : status.path("containerStatuses")) {
        total++;
        if (containerStatus.path("ready").asBoolean(false)) {
          ready++;
        }
        restarts += containerStatus.path("restartCount").asInt(0);
        containers.add(containerStatus.path("name").asText());
      }
      Map<String, String> pod = new LinkedHashMap<>();
      pod.put("name", metadata.path("name").asText());
      pod.put("status", status.path("phase").asText("Unavailable"));
      pod.put("ready", ready + "/" + total);
      pod.put("restarts", String.valueOf(restarts));
      pod.put("node", status.path("hostIP").asText("Unavailable"));
      pod.put("createdOn", formatAge(metadata.path("creationTimestamp").asText("")));
      pod.put("containers", String.join(",", containers));
      pods.add(pod);
    }
    return pods;
  }

  private List<Map<String, String>> getDeployments(String namespace)
      throws IOException, InterruptedException {
    JsonNode root = runKubectlJson("get", "deployments", "-n", namespace, "-o", "json");
    List<Map<String, String>> deployments = new ArrayList<>();
    for (JsonNode item : root.path("items")) {
      JsonNode metadata = item.path("metadata");
      JsonNode status = item.path("status");
      Map<String, String> deployment = new LinkedHashMap<>();
      deployment.put("name", metadata.path("name").asText());
      deployment.put("ready", status.path("readyReplicas").asInt(0) + "/"
          + item.path("spec").path("replicas").asInt(0));
      deployment.put("available", String.valueOf(status.path("availableReplicas").asInt(0)));
      deployment.put("createdOn", formatAge(metadata.path("creationTimestamp").asText("")));
      deployments.add(deployment);
    }
    return deployments;
  }

  private List<Map<String, String>> getServices(String namespace)
      throws IOException, InterruptedException {
    JsonNode root = runKubectlJson("get", "services", "-n", namespace, "-o", "json");
    List<Map<String, String>> services = new ArrayList<>();
    for (JsonNode item : root.path("items")) {
      JsonNode metadata = item.path("metadata");
      JsonNode spec = item.path("spec");
      Map<String, String> service = new LinkedHashMap<>();
      service.put("name", metadata.path("name").asText());
      service.put("type", spec.path("type").asText("Unavailable"));
      service.put("clusterIp", spec.path("clusterIP").asText("Unavailable"));
      service.put("ports", servicePorts(spec.path("ports")));
      service.put("createdOn", formatAge(metadata.path("creationTimestamp").asText("")));
      services.add(service);
    }
    return services;
  }

  private JsonNode runKubectlJson(String... arguments) throws IOException, InterruptedException {
    List<String> command = new ArrayList<>();
    command.add("kubectl");
    command.add("--request-timeout=5s");
    command.addAll(List.of(arguments));
    String output = runCommand(command.toArray(String[]::new));
    return objectMapper.readTree(output);
  }

  private String runCommand(String... command) throws IOException, InterruptedException {
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();
    boolean completed = process.waitFor(8, TimeUnit.SECONDS);
    if (!completed) {
      process.destroyForcibly();
      throw new IOException(String.join(" ", command) + " timed out after 8 seconds");
    }
    StringBuilder output = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
      }
    }
    int exitCode = process.exitValue();
    if (exitCode != 0) {
      throw new IOException(String.join(" ", command) + " exited with code " + exitCode + ": "
          + output.toString().trim());
    }
    return output.toString().trim();
  }

  private String servicePorts(JsonNode ports) {
    List<String> values = new ArrayList<>();
    for (JsonNode port : ports) {
      values.add(port.path("port").asText() + ":" + port.path("targetPort").asText());
    }
    return String.join(", ", values);
  }

  private String formatAge(String value) {
    if (value == null || value.isBlank()) {
      return "Unavailable";
    }
    try {
      return Instant.parse(value).toString().substring(0, 16);
    } catch (Exception e) {
      return value.length() > 16 ? value.substring(0, 16) : value;
    }
  }

  private String requireValue(String value, String label) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(label + " is required.");
    }
    return value.trim();
  }

  private String requireServiceType(String value) {
    String type = requireValue(value, "Service type");
    if (!List.of("ClusterIP", "NodePort", "LoadBalancer").contains(type)) {
      throw new IllegalArgumentException("Service type must be ClusterIP, NodePort, or LoadBalancer.");
    }
    return type;
  }
}
