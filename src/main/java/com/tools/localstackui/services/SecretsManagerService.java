package com.tools.localstackui.services;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.DeleteSecretRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.ListSecretsRequest;
import com.amazonaws.services.secretsmanager.model.ListSecretsResult;
import com.amazonaws.services.secretsmanager.model.PutSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SecretsManagerService {

  private static final DateTimeFormatter UI_TIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  @Autowired
  AWSSecretsManager secretsManager;

  public List<Map<String, String>> getSecretDetails() {
    List<Map<String, String>> secrets = new ArrayList<>();
    String nextToken = null;
    do {
      ListSecretsRequest request = new ListSecretsRequest().withNextToken(nextToken);
      ListSecretsResult result = secretsManager.listSecrets(request);
      for (SecretListEntry secret : result.getSecretList()) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("name", secret.getName());
        details.put("arn", secret.getARN());
        details.put("address", secret.getARN());
        details.put("description", secret.getDescription() == null ? "" : secret.getDescription());
        details.put("createdOn", formatDate(secret.getCreatedDate()));
        details.put("lastChangedOn", formatDate(secret.getLastChangedDate()));
        secrets.add(details);
      }
      nextToken = result.getNextToken();
    } while (nextToken != null);
    secrets.sort(Comparator.comparing(secret -> secret.get("name"), String.CASE_INSENSITIVE_ORDER));
    return secrets;
  }

  public Map<String, String> getSecretValue(String secretId) {
    String safeSecretId = requireValue(secretId, "Secret id");
    var result = secretsManager.getSecretValue(new GetSecretValueRequest().withSecretId(safeSecretId));
    Map<String, String> response = new LinkedHashMap<>();
    response.put("name", result.getName());
    response.put("arn", result.getARN());
    response.put("versionId", result.getVersionId());
    response.put("value", result.getSecretString() == null ? "" : result.getSecretString());
    response.put("createdOn", formatDate(result.getCreatedDate()));
    return response;
  }

  public Map<String, String> createSecret(String secretName, String secretValue, String description) {
    String safeName = requireValue(secretName, "Secret name");
    String safeValue = requireValue(secretValue, "Secret value");
    CreateSecretRequest request = new CreateSecretRequest()
        .withName(safeName)
        .withSecretString(safeValue);
    if (description != null && !description.isBlank()) {
      request.withDescription(description.trim());
    }
    var result = secretsManager.createSecret(request);
    Map<String, String> response = new LinkedHashMap<>();
    response.put("name", safeName);
    response.put("arn", result.getARN());
    response.put("message", "Created secret " + safeName);
    return response;
  }

  public Map<String, String> updateSecretValue(String secretId, String secretValue) {
    String safeSecretId = requireValue(secretId, "Secret id");
    String safeValue = requireValue(secretValue, "Secret value");
    var result = secretsManager.putSecretValue(new PutSecretValueRequest()
        .withSecretId(safeSecretId)
        .withSecretString(safeValue));
    Map<String, String> response = new LinkedHashMap<>();
    response.put("arn", result.getARN());
    response.put("name", result.getName());
    response.put("versionId", result.getVersionId());
    response.put("message", "Updated secret " + result.getName());
    return response;
  }

  public String deleteSecret(String secretId) {
    String safeSecretId = requireValue(secretId, "Secret id");
    secretsManager.deleteSecret(new DeleteSecretRequest()
        .withSecretId(safeSecretId)
        .withForceDeleteWithoutRecovery(true));
    return "Deleted secret " + safeSecretId;
  }

  private String requireValue(String value, String label) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(label + " is required.");
    }
    return value.trim();
  }

  private String formatDate(Date date) {
    if (date == null) {
      return "Unavailable";
    }
    return date.toInstant().atZone(ZoneId.systemDefault()).format(UI_TIME_FORMAT);
  }
}
