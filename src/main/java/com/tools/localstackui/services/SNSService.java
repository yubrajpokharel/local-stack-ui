package com.tools.localstackui.services;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.ListSubscriptionsResult;
import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.Subscription;
import com.amazonaws.services.sns.model.Topic;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class SNSService {

  @Autowired
  AmazonSNS amazonSNS;

  public List<String> getSubscription() {
    ListSubscriptionsResult result = amazonSNS.listSubscriptions();
    return result.getSubscriptions().stream().map(x -> x.getEndpoint())
        .collect(toList());
  }

  public String createTopic(String topicName){
    return amazonSNS.createTopic(topicName).getTopicArn();
  }

  public List<String> getListOfTopics() {
    ListTopicsResult list = amazonSNS.listTopics();
    return list.getTopics().stream().map(Topic::getTopicArn).collect(toList());
  }

  public Map<String, String> getSubscriptionByTopicName(String topicName){
    Map<String, String> response = new HashMap<>();
    List<Subscription> subscriptionList = amazonSNS.listSubscriptionsByTopic(topicName).getSubscriptions();
    if (!isEmpty(subscriptionList)) {
      for(Subscription subscription: subscriptionList){
        response.put(subscription.getEndpoint(), subscription.getSubscriptionArn());
      }
    }
    return response;
  }

  public String sentMessage(String topicArn, String msg){
    final PublishRequest publishRequest = new PublishRequest(topicArn, msg);
    final PublishResult publishResponse = amazonSNS.publish(publishRequest);
    return publishResponse.getMessageId();
  }

  public String deleteTopic(String topicArn){
    amazonSNS.deleteTopic(topicArn);
    return "success";
  }


}
