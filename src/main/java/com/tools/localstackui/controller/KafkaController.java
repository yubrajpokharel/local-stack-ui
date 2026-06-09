package com.tools.localstackui.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.tools.localstackui.services.KafkaService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KafkaController {

  @Autowired
  KafkaService kafkaService;

  @GetMapping(value = "/kafka/status", produces = APPLICATION_JSON_VALUE)
  public Map<String, Object> getStatus() {
    return kafkaService.getStatus();
  }

  @GetMapping(value = "/kafka/topics", produces = APPLICATION_JSON_VALUE)
  public List<Map<String, String>> getTopics() throws Exception {
    return kafkaService.getTopicDetails();
  }

  @GetMapping(value = "/kafka/topics/{topicName}/details", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> getTopic(@PathVariable String topicName) throws Exception {
    return kafkaService.getTopicDetail(topicName);
  }

  @GetMapping(value = "/kafka/topics/{topicName}/partitions", produces = APPLICATION_JSON_VALUE)
  public List<Map<String, String>> getTopicPartitions(@PathVariable String topicName)
      throws Exception {
    return kafkaService.getTopicPartitions(topicName);
  }

  @PostMapping(value = "/kafka/topics", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> createTopic(@RequestParam String topicName,
      @RequestParam(defaultValue = "1") int partitions,
      @RequestParam(defaultValue = "1") short replicationFactor) {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.put("message", kafkaService.createTopic(topicName, partitions, replicationFactor));
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }

  @PostMapping(value = "/kafka/topics/{topicName}/messages", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> publishMessage(@PathVariable String topicName,
      @RequestBody String message) {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.put("message", kafkaService.publishMessage(topicName, message));
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }

  @PostMapping(value = "/kafka/start", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> startKafka() {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.put("message", kafkaService.startKafka());
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }

  @PostMapping(value = "/kafka/stop", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> stopKafka() {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.put("message", kafkaService.stopKafka());
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }
}
