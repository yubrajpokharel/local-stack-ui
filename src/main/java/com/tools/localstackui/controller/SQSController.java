package com.tools.localstackui.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import com.amazonaws.services.sqs.model.Message;
import com.tools.localstackui.services.SQSService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SQSController {

  @Autowired
  SQSService sqsService;

  @Value("${aws.region.name}")
  private String region;

  @RequestMapping(value = "/sqs-message/{queueUrl}", method = GET)
  public String getMessage(@PathVariable("queueUrl") String queueUrl, Model model) {
    System.out.println(queueUrl);
    String fullQueueUrl =
        "http://sqs." + region + ".localhost.localstack.cloud:4566/000000000000/" + queueUrl;
    List<Message> messages =  sqsService.getSqsMessages(fullQueueUrl);
    messages.forEach(System.out::println);
    model.addAttribute("sqsQueue", queueUrl);
    model.addAttribute("sqsQueueUrl", fullQueueUrl);
    model.addAttribute("sqsCreatedOn", sqsService.getCreatedOn(fullQueueUrl));
    model.addAttribute("messages", messages);
    return "indSqsPage";
  }
}
