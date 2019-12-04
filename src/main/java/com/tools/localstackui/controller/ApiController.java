package com.tools.localstackui.controller;


import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.amazonaws.services.sqs.model.Message;
import com.tools.localstackui.services.SNSService;
import com.tools.localstackui.services.SQSService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

  @Autowired
  SQSService sqsService;

  @Autowired
  SNSService snsService;

  private static final String LOCAL_SQS_URL = "http://localhost:4576/queue/";


  /*****************************************************
   * Messages starts
   *****************************************************/

  @GetMapping(value = "/message", produces = APPLICATION_JSON_VALUE)
  public List<String> getListOfMessage() {
    List<Message> messages = sqsService.getSqsMessages();
    List<String> messageIds = messages.stream().map(Message::getMessageId)
        .collect(toList());
    return !isEmpty(messageIds) ? messageIds : emptyList();
  }

  @GetMapping(value = "/message/{id}", produces = APPLICATION_JSON_VALUE)
  public Message getIndividualMessage(@PathVariable("id") String id) {
    List<Message> messages = sqsService.getSqsMessages();
    Message message = messages.stream()
        .filter(x -> x.getMessageId().equalsIgnoreCase(id))
        .findFirst().orElse(null);
    return message;
  }

  /*****************************************************
   * Messages ends
   *****************************************************/


  /*****************************************************
   * SNS Starts
   *****************************************************/

  @PostMapping(value = "/sns/createTopic/{topicName}")
  public String createTopic(@PathVariable("topicName") String topicName) {
    return snsService.createTopic(topicName);
  }

  @GetMapping(value = "/sns-topics", produces = APPLICATION_JSON_VALUE)
  public List<String> getSNSTopics() {
    return snsService.getListOfTopics();
  }

  @PostMapping(value = "/sns-topics/sendMessage/{topicArn}", consumes = APPLICATION_JSON_VALUE)
  public String sendMessage(@PathVariable("topicArn") String topicArn,
      @RequestBody String message) {
    return snsService.sentMessage(topicArn, message);
  }

  @PostMapping(value = "/deleteTopic/{topicArn}")
  public String delete(@PathVariable("topicArn") String topicArn) {
    return snsService.deleteTopic(topicArn);
  }


  /*****************************************************
   * SQS Starts
   *****************************************************/

  @PostMapping(value = "/sqs/createQueue/{queueName}")
  public String createQueue(@PathVariable("queueName") String queueName) {
    return sqsService.createQueue(queueName);
  }

  @GetMapping(value = "/sqs", produces = APPLICATION_JSON_VALUE)
  public List<String> getSQSTopics() {
    return sqsService.getQueues();
  }

  @PostMapping(value = "/deleteQueue/{queueUrl}")
  public String deleteQueue(@PathVariable("queueUrl") String queueUrl) {
    return sqsService.delete(LOCAL_SQS_URL+queueUrl);
  }

  @PostMapping(value = "/subscribe/{queueUrl}/{topicArn}")
  public String subscribe(@PathVariable("queueUrl") String queueUrl,
      @PathVariable("topicArn") String topicArn) {
    return sqsService.subscribeQueueToTopic(LOCAL_SQS_URL+queueUrl, topicArn);
  }

  @DeleteMapping(value = "/unsubscribe/{subscriptionArn}")
  public String unsubscribe(@PathVariable("subscriptionArn") String subscriptionArn) {
    return sqsService.unSubscribeQueueToTopic(subscriptionArn);
  }
}
