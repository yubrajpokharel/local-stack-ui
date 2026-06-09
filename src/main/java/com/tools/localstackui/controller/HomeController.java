package com.tools.localstackui.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller()
public class HomeController {

  @Value("${gcp.project.id:localstack-ui}")
  private String gcpProjectId;

  @RequestMapping(value = "/", method = GET)
  public String home(Model model) {
    return "index";
  }

  @RequestMapping(value = "/messaging", method = GET)
  public String messaging(Model model) {
    return "messaging";
  }

  @RequestMapping(value = "/s3", method = GET)
  public String s3s(Model model) {
    return "s3s";
  }

  @RequestMapping(value = "/redis", method = GET)
  public String redis(Model model) {
    return "redis";
  }

  @RequestMapping(value = "/mock-http", method = GET)
  public String mockHttp(Model model) {
    return "mockHttp";
  }

  @RequestMapping(value = "/mongodb", method = GET)
  public String mongodb(Model model) {
    return "mongodb";
  }

  @RequestMapping(value = "/kafka", method = GET)
  public String kafka(Model model) {
    return "kafka";
  }

  @RequestMapping(value = "/kafka/topics/{topicName}", method = GET)
  public String kafkaTopic(@PathVariable String topicName, Model model) {
    model.addAttribute("topicName", topicName);
    model.addAttribute("topicAddress", "localhost:9092/" + topicName);
    model.addAttribute("topicCreatedOn", "Unavailable");
    return "kafkaTopic";
  }

  @RequestMapping(value = "/gcp", method = GET)
  public String gcp(Model model) {
    return "gcpPubSub";
  }

  @RequestMapping(value = "/gcp/pubsub", method = GET)
  public String gcpPubSub(Model model) {
    return "gcpPubSub";
  }

  @RequestMapping(value = "/gcp/pubsub/topics/{topicName}", method = GET)
  public String gcpPubSubTopic(@PathVariable String topicName, Model model) {
    model.addAttribute("topicName", topicName);
    model.addAttribute("topicAddress", "projects/" + gcpProjectId + "/topics/" + topicName);
    model.addAttribute("topicCreatedOn", "Unavailable");
    return "gcpPubSubTopic";
  }

  @RequestMapping(value = "/gcp/pubsub/subscriptions/{subscriptionName}", method = GET)
  public String gcpPubSubSubscription(@PathVariable String subscriptionName, Model model) {
    model.addAttribute("subscriptionName", subscriptionName);
    model.addAttribute("subscriptionAddress",
        "projects/" + gcpProjectId + "/subscriptions/" + subscriptionName);
    model.addAttribute("subscriptionCreatedOn", "Unavailable");
    return "gcpPubSubSubscription";
  }

  @RequestMapping(value = "/gcp/storage", method = GET)
  public String gcpStorage(Model model) {
    return "gcpStorage";
  }

  @RequestMapping(value = "/gcp/firestore", method = GET)
  public String gcpFirestore(Model model) {
    return "gcpFirestore";
  }

}
