package com.tools.localstackui.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.tools.localstackui.services.MongoDbService;
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
public class MongoDbController {

  @Autowired
  MongoDbService mongoDbService;

  @GetMapping(value = "/mongodb/status", produces = APPLICATION_JSON_VALUE)
  public Map<String, Object> getStatus() {
    return mongoDbService.getStatus();
  }

  @GetMapping(value = "/mongodb/databases", produces = APPLICATION_JSON_VALUE)
  public List<String> getDatabases() {
    return mongoDbService.getDatabases();
  }

  @GetMapping(value = "/mongodb/databases/{databaseName}/collections", produces = APPLICATION_JSON_VALUE)
  public List<String> getCollections(@PathVariable String databaseName) {
    return mongoDbService.getCollections(databaseName);
  }

  @PostMapping(value = "/mongodb/databases", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> createDatabase(@RequestParam String databaseName,
      @RequestParam(required = false) String collectionName) {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.put("message", mongoDbService.createDatabase(databaseName, collectionName));
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }

  @PostMapping(value = "/mongodb/databases/{databaseName}/collections", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> createCollection(@PathVariable String databaseName,
      @RequestParam String collectionName) {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.put("message", mongoDbService.createCollection(databaseName, collectionName));
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }

  @PostMapping(value = "/mongodb/start", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> startMongoDb() {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.put("message", mongoDbService.startMongoDb());
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }

  @PostMapping(value = "/mongodb/stop", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> stopMongoDb() {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.put("message", mongoDbService.stopMongoDb());
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }
}
