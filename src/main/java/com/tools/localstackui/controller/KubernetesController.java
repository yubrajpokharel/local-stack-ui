package com.tools.localstackui.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import com.tools.localstackui.services.KubernetesService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KubernetesController {

  @Autowired
  KubernetesService kubernetesService;

  @GetMapping(value = "/kubernetes/{provider}/status", produces = APPLICATION_JSON_VALUE)
  public Map<String, Object> getStatus(@PathVariable String provider) {
    return kubernetesService.getStatus(provider);
  }

  @GetMapping(value = "/kubernetes/{provider}/namespaces", produces = APPLICATION_JSON_VALUE)
  public List<Map<String, String>> getNamespaces(@PathVariable String provider) throws Exception {
    return kubernetesService.getNamespaces();
  }

  @GetMapping(value = "/kubernetes/{provider}/overview", produces = APPLICATION_JSON_VALUE)
  public Map<String, Object> getOverview(@PathVariable String provider,
      @RequestParam(defaultValue = "default") String namespace) throws Exception {
    return kubernetesService.getNamespaceOverview(namespace);
  }

  @GetMapping(value = "/kubernetes/{provider}/events", produces = TEXT_PLAIN_VALUE)
  public String getEvents(@PathVariable String provider,
      @RequestParam(defaultValue = "default") String namespace) throws Exception {
    return kubernetesService.getEvents(namespace);
  }

  @GetMapping(value = "/kubernetes/{provider}/logs", produces = TEXT_PLAIN_VALUE)
  public String getLogs(@PathVariable String provider,
      @RequestParam String namespace,
      @RequestParam String pod,
      @RequestParam(required = false) String container) throws Exception {
    return kubernetesService.getLogs(namespace, pod, container);
  }

  @PostMapping(value = "/kubernetes/{provider}/services", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> createService(@PathVariable String provider,
      @RequestParam String namespace,
      @RequestParam String deploymentName,
      @RequestParam String serviceName,
      @RequestParam(defaultValue = "ClusterIP") String serviceType,
      @RequestParam(defaultValue = "8080") int port,
      @RequestParam(defaultValue = "8080") int targetPort) {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.put("message", kubernetesService.createService(namespace, deploymentName,
          serviceName, serviceType, port, targetPort));
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }

  @GetMapping(value = "/kubernetes/{provider}/commands", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> getCommands(@PathVariable String provider) {
    Map<String, String> commands = new LinkedHashMap<>();
    if ("gcp".equalsIgnoreCase(provider)) {
      commands.put("connect",
          "gcloud container clusters get-credentials <cluster-name> --region <region> --project <project-id>");
      commands.put("listClusters",
          "gcloud container clusters list --project <project-id>");
    } else {
      commands.put("connect",
          "aws eks update-kubeconfig --region <region> --name <cluster-name>");
      commands.put("listClusters",
          "aws eks list-clusters --region <region>");
    }
    commands.put("contexts", "kubectl config get-contexts");
    commands.put("namespaces", "kubectl get namespaces");
    commands.put("pods", "kubectl get pods -n <namespace>");
    commands.put("logs", "kubectl logs -n <namespace> <pod> --tail=200");
    commands.put("events", "kubectl get events -n <namespace> --sort-by=.lastTimestamp");
    return commands;
  }
}
