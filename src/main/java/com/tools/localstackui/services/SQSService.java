package com.tools.localstackui.services;

import static java.util.Arrays.asList;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SQSService {

  @Autowired
  AmazonSQS amazonSQS;

  @Autowired
  AmazonSNS amazonSNS;

  private String sqsName;

  public String createQueue(String queueName) {
    return amazonSQS.createQueue(queueName).getQueueUrl();
  }

  public List<String> getQueues() {
    return amazonSQS.listQueues().getQueueUrls();
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

  public List<Message> getSqsMessages() {
    ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(sqsName)
        .withWaitTimeSeconds(10)
        .withMaxNumberOfMessages(10);
    List<Message> sqsMessages = amazonSQS.receiveMessage(receiveMessageRequest).getMessages();

    return sqsMessages;
  }

  public String delete(String queueUrl) {
    amazonSQS.deleteQueue(queueUrl);
    return "success";
  }

}
