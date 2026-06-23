package com.tools.localstackui.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import com.tools.localstackui.services.SecretsManagerService;
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
public class SecretsManagerController {

  @Autowired
  SecretsManagerService secretsManagerService;

  @GetMapping(value = "/secrets-manager/secrets", produces = APPLICATION_JSON_VALUE)
  public List<Map<String, String>> getSecrets() {
    return secretsManagerService.getSecretDetails();
  }

  @GetMapping(value = "/secrets-manager/secrets/value", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> getSecretValue(@RequestParam String secretId) {
    return secretsManagerService.getSecretValue(secretId);
  }

  @PostMapping(value = "/secrets-manager/secrets", consumes = TEXT_PLAIN_VALUE,
      produces = APPLICATION_JSON_VALUE)
  public Map<String, String> createSecret(@RequestParam String secretName,
      @RequestParam(required = false) String description,
      @RequestBody String secretValue) {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.putAll(secretsManagerService.createSecret(secretName, secretValue, description));
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }

  @PutMapping(value = "/secrets-manager/secrets/value", consumes = TEXT_PLAIN_VALUE,
      produces = APPLICATION_JSON_VALUE)
  public Map<String, String> updateSecretValue(@RequestParam String secretId,
      @RequestBody String secretValue) {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.putAll(secretsManagerService.updateSecretValue(secretId, secretValue));
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }

  @DeleteMapping(value = "/secrets-manager/secrets", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> deleteSecret(@RequestParam String secretId) {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.put("message", secretsManagerService.deleteSecret(secretId));
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }
}
