package com.tools.localstackui.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import com.tools.localstackui.services.RabbitMqService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RabbitMqController {

  @Autowired
  RabbitMqService rabbitMqService;

  @GetMapping(value = "/rabbitmq/status", produces = APPLICATION_JSON_VALUE)
  public Map<String, Object> getStatus() {
    return rabbitMqService.getStatus();
  }

  @PostMapping(value = "/rabbitmq/start", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> startRabbitMq() {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.put("message", rabbitMqService.startRabbitMq());
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }

  @PostMapping(value = "/rabbitmq/stop", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> stopRabbitMq() {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.put("message", rabbitMqService.stopRabbitMq());
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }

  @GetMapping(value = "/rabbitmq/queues", produces = APPLICATION_JSON_VALUE)
  public List<Map<String, String>> getQueues() throws Exception {
    return rabbitMqService.getQueues();
  }

  @PostMapping(value = "/rabbitmq/queues", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> createQueue(@RequestParam String queueName,
      @RequestParam(defaultValue = "true") boolean durable,
      @RequestParam(defaultValue = "false") boolean autoDelete,
      @RequestParam(defaultValue = "classic") String queueType) {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.put("message", rabbitMqService.createQueue(queueName, durable, autoDelete,
          queueType));
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }

  @DeleteMapping(value = "/rabbitmq/queues/{queueName}", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> deleteQueue(@PathVariable String queueName) {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.put("message", rabbitMqService.deleteQueue(queueName));
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }

  @PostMapping(value = "/rabbitmq/queues/{queueName}/messages", consumes = TEXT_PLAIN_VALUE,
      produces = APPLICATION_JSON_VALUE)
  public Map<String, String> publishMessage(@PathVariable String queueName,
      @RequestBody String message) {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.put("message", rabbitMqService.publishMessage(queueName, message));
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }

  @GetMapping(value = "/rabbitmq/queues/{queueName}/messages", produces = APPLICATION_JSON_VALUE)
  public List<Map<String, String>> peekMessages(@PathVariable String queueName,
      @RequestParam(defaultValue = "10") int count) throws Exception {
    return rabbitMqService.peekMessages(queueName, count);
  }
}
