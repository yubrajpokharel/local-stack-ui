package com.tools.localstackui.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.tools.localstackui.services.LocalStackService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LocalStackController {

  @Autowired
  LocalStackService localStackService;

  @GetMapping(value = "/localstack/status", produces = APPLICATION_JSON_VALUE)
  public Map<String, Object> getStatus() {
    return localStackService.getStatus();
  }

  @PostMapping(value = "/localstack/start", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> startLocalStack() {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.put("message", localStackService.startLocalStack());
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }

  @PostMapping(value = "/localstack/stop", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> stopLocalStack() {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.put("message", localStackService.stopLocalStack());
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }
}
