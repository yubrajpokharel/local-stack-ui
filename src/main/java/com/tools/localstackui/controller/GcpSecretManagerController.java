package com.tools.localstackui.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import com.tools.localstackui.services.GcpSecretManagerService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GcpSecretManagerController {

  @Autowired
  GcpSecretManagerService gcpSecretManagerService;

  @GetMapping(value = "/gcp/secret-manager/status", produces = APPLICATION_JSON_VALUE)
  public Map<String, Object> getStatus() {
    return gcpSecretManagerService.getStatus();
  }

  @GetMapping(value = "/gcp/secret-manager/secrets", produces = APPLICATION_JSON_VALUE)
  public List<Map<String, String>> getSecrets() throws Exception {
    return gcpSecretManagerService.getSecretDetails();
  }

  @GetMapping(value = "/gcp/secret-manager/secrets/value", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> getSecretValue(@RequestParam String secretId) throws Exception {
    return gcpSecretManagerService.getSecretValue(secretId);
  }

  @PostMapping(value = "/gcp/secret-manager/secrets", consumes = TEXT_PLAIN_VALUE,
      produces = APPLICATION_JSON_VALUE)
  public Map<String, String> createSecret(@RequestParam String secretId,
      @RequestBody String secretValue) {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.putAll(gcpSecretManagerService.createSecret(secretId, secretValue));
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }

  @PutMapping(value = "/gcp/secret-manager/secrets/value", consumes = TEXT_PLAIN_VALUE,
      produces = APPLICATION_JSON_VALUE)
  public Map<String, String> updateSecretValue(@RequestParam String secretId,
      @RequestBody String secretValue) {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.putAll(gcpSecretManagerService.updateSecretValue(secretId, secretValue));
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }

  @DeleteMapping(value = "/gcp/secret-manager/secrets", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> deleteSecret(@RequestParam String secretId) {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.put("message", gcpSecretManagerService.deleteSecret(secretId));
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }
}
