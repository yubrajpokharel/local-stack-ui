package com.tools.localstackui.services;

import static java.util.Arrays.asList;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sns.model.UnsubscribeResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SQSService {

  private static final DateTimeFormatter DISPLAY_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm").withZone(ZoneId.systemDefault());

  @Autowired
  AmazonSQS amazonSQS;

  @Autowired
  AmazonSNS amazonSNS;

  private String sqsName;

  public String createQueue(String queueName) {
    return amazonSQS.createQueue(queueName).getQueueUrl();
  }

  public String createQueue(String queueName, String queueType) {
    if (!"FIFO".equalsIgnoreCase(queueType)) {
      return createQueue(queueName);
    }

    String fifoQueueName = queueName.endsWith(".fifo") ? queueName : queueName + ".fifo";
    Map<String, String> attributes = new HashMap<>();
    attributes.put(QueueAttributeName.FifoQueue.toString(), "true");
    attributes.put(QueueAttributeName.ContentBasedDeduplication.toString(), "true");
    CreateQueueRequest request = new CreateQueueRequest(fifoQueueName)
        .withAttributes(attributes);
    return amazonSQS.createQueue(request).getQueueUrl();
  }

  public List<String> getQueues() {
    return amazonSQS.listQueues().getQueueUrls();
  }

  public List<Map<String, String>> getQueueDetails() {
    return getQueues().stream().map(queueUrl -> {
      Map<String, String> queueDetails = new LinkedHashMap<>();
      String queueName = queueUrl.substring(queueUrl.lastIndexOf("/") + 1);
      queueDetails.put("name", queueName);
      queueDetails.put("url", queueUrl);
      queueDetails.put("type", getQueueType(queueUrl));
      queueDetails.put("createdOn", getCreatedOn(queueUrl));
      return queueDetails;
    }).toList();
  }

  public String subscribeQueueToTopic(String queueUrl, String topicArn) {
    List<String> sqsAttrNames = asList(QueueAttributeName.QueueArn.toString(),
        QueueAttributeName.Policy.toString());
    Map<String, String> sqsAttrs = amazonSQS.getQueueAttributes(queueUrl, sqsAttrNames)
        .getAttributes();
    String sqsQueueArn = (String) sqsAttrs.get(QueueAttributeName.QueueArn.toString());
    SubscribeResult subscribeResult = amazonSNS.subscribe(topicArn, "sqs", sqsQueueArn);
    return subscribeResult.getSubscriptionArn();
  }

  public String unSubscribeQueueToTopic(String subscriptionArn) {
    UnsubscribeResult unSubscribeResult = amazonSNS.unsubscribe(subscriptionArn);
    return "Successfully removed subscription";
  }

  public List<Message> getSqsMessages() {
    ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(sqsName)
        .withWaitTimeSeconds(10)
        .withMaxNumberOfMessages(10)
        .withVisibilityTimeout(0);
    List<Message> sqsMessages = amazonSQS.receiveMessage(receiveMessageRequest).getMessages();

    return sqsMessages;
  }

  public List<Message> getSqsMessages(String queueName) {
    ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueName)
        .withMaxNumberOfMessages(10)
        .withVisibilityTimeout(0);
    List<Message> sqsMessages = amazonSQS.receiveMessage(receiveMessageRequest).getMessages();

    return sqsMessages;
  }

  public String sendMessage(String queueUrl, String message, String messageGroupId,
      String messageDeduplicationId) {
    SendMessageRequest request = new SendMessageRequest(queueUrl, message);
    if ("FIFO".equals(getQueueType(queueUrl))) {
      request.withMessageGroupId(
          messageGroupId == null || messageGroupId.isBlank() ? "default" : messageGroupId);
      if (messageDeduplicationId != null && !messageDeduplicationId.isBlank()) {
        request.withMessageDeduplicationId(messageDeduplicationId);
      }
    }
    SendMessageResult sendMessageResult = amazonSQS.sendMessage(request);
    return sendMessageResult.getMessageId();
  }

  public String delete(String queueUrl) {
    amazonSQS.deleteQueue(queueUrl);
    return "success";
  }

  public String getCreatedOn(String queueUrl) {
    Map<String, String> attributes = amazonSQS
        .getQueueAttributes(queueUrl, asList(QueueAttributeName.CreatedTimestamp.toString()))
        .getAttributes();
    String timestamp = attributes.get(QueueAttributeName.CreatedTimestamp.toString());
    if (timestamp == null) {
      return "";
    }
    return DISPLAY_TIME_FORMATTER.format(Instant.ofEpochSecond(Long.parseLong(timestamp)));
  }

  public String getQueueType(String queueUrl) {
    Map<String, String> attributes = amazonSQS
        .getQueueAttributes(queueUrl, asList(QueueAttributeName.FifoQueue.toString()))
        .getAttributes();
    return "true".equalsIgnoreCase(attributes.get(QueueAttributeName.FifoQueue.toString()))
        ? "FIFO" : "Standard";
  }

}
