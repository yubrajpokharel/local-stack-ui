package com.tools.localstackui.controller;


import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.sqs.model.Message;
import com.tools.localstackui.services.S3Service;
import com.tools.localstackui.services.SNSService;
import com.tools.localstackui.services.SQSService;
import com.tools.localstackui.services.RedisService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ApiController {

  @Autowired
  SQSService sqsService;

  @Autowired
  SNSService snsService;

  @Autowired
  S3Service s3Service;

  @Autowired
  RedisService redisService;

  private static final String LOCAL_SQS_URL = "http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/";
  private static final String LOCAL_S3_URL = "http://s3.us-east-1.localhost.localstack.cloud:4566";


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

  @GetMapping(value = "/sqs/details", produces = APPLICATION_JSON_VALUE)
  public List<Map<String, String>> getSQSDetails() {
    return sqsService.getQueueDetails();
  }

  @PostMapping(value = "/sqs/sendMessage/{queueUrl}", consumes = MediaType.TEXT_PLAIN_VALUE)
  public String sendSqsMessage(@PathVariable("queueUrl") String queueUrl,
      @RequestBody String message) {
    return sqsService.sendMessage(LOCAL_SQS_URL + queueUrl, message);
  }

  @PostMapping(value = "/deleteQueue/{queueUrl}")
  public String deleteQueue(@PathVariable("queueUrl") String queueUrl) {
    return sqsService.delete(LOCAL_SQS_URL + queueUrl);
  }

  @PostMapping(value = "/subscribe/{queueUrl}/{topicArn}")
  public String subscribe(@PathVariable("queueUrl") String queueUrl,
      @PathVariable("topicArn") String topicArn) {
    return sqsService.subscribeQueueToTopic(LOCAL_SQS_URL + queueUrl, topicArn);
  }

  @DeleteMapping(value = "/unsubscribe/{subscriptionArn}")
  public String unsubscribe(@PathVariable("subscriptionArn") String subscriptionArn) {
    return sqsService.unSubscribeQueueToTopic(subscriptionArn);
  }

  /*****************************************************
   * S3 Starts
   *****************************************************/

  @GetMapping(value = "/s3-buckets", produces = APPLICATION_JSON_VALUE)
  public List<Bucket> getS3Buckets() {
    return s3Service.getListofBuckets();
  }

  @PostMapping(value = "/s3-buckets/create/{bucketName}", produces = APPLICATION_JSON_VALUE)
  public Bucket getS3Buckets(@PathVariable("bucketName") String bucketName) {
    return s3Service.createS3Bucket(bucketName);
  }

  @PostMapping(value = "/s3-buckets/delete/{bucketName}")
  public String deleteBucket(@PathVariable("bucketName") String bucketName) {
    return s3Service.deleteBucket(bucketName);
  }

  @GetMapping(value = "/s3-buckets/get/{bucketName}")
  public List<String> getBucketContent(@PathVariable("bucketName") String bucketName) {
    return s3Service.listInternal(bucketName)
        .stream().map(x -> x.getKey())
        .collect(toList());
  }

  @PostMapping("/s3-buckets/uploadFile/{bucketName}")
  public String uploadFile(@RequestPart(value = "file") MultipartFile file,
      @PathVariable(value = "bucketName") String bucketName) {
    return this.s3Service.uploadFile(file, bucketName);
  }

  @DeleteMapping("/s3-buckets/deleteFile/{bucketName}/{fileName}")
  public String deleteFile(@PathVariable(value = "bucketName") String bucketName,
      @PathVariable(value = "fileName") String fileName) {
    return this.s3Service
        .deleteFileFromS3Bucket(LOCAL_S3_URL + bucketName + "/" + fileName, bucketName);
  }

  /*****************************************************
   * Redis Starts
   *****************************************************/

  @GetMapping(value = "/redis/health", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> getRedisHealth() {
    Map<String, String> response = new LinkedHashMap<>();
    response.put("status", redisService.ping());
    return response;
  }

  @GetMapping(value = "/redis/keys", produces = APPLICATION_JSON_VALUE)
  public List<String> getRedisKeys() {
    return redisService.getKeys();
  }

  @GetMapping(value = "/redis/key-details", produces = APPLICATION_JSON_VALUE)
  public List<Map<String, String>> getRedisKeyDetails() {
    return redisService.getKeyDetails();
  }

  @GetMapping(value = "/redis/value/{key}", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> getRedisValue(@PathVariable("key") String key) {
    Map<String, String> response = new LinkedHashMap<>();
    String value = redisService.getValue(key);
    response.put("key", key);
    response.put("value", value == null ? "" : value);
    return response;
  }

  @PostMapping(value = "/redis/value/{key}", consumes = MediaType.TEXT_PLAIN_VALUE,
      produces = APPLICATION_JSON_VALUE)
  public Map<String, String> setRedisValue(@PathVariable("key") String key,
      @RequestBody String value) {
    redisService.setValue(key, value);
    return getRedisValue(key);
  }

  @DeleteMapping(value = "/redis/value/{key}", produces = APPLICATION_JSON_VALUE)
  public Map<String, Object> deleteRedisValue(@PathVariable("key") String key) {
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("key", key);
    response.put("deleted", redisService.delete(key));
    return response;
  }
}
