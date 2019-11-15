package com.tools.localstackui.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import com.amazonaws.services.sqs.model.Message;
import com.tools.localstackui.services.SQSService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SQSController {

  @Autowired
  SQSService sqsService;

  @RequestMapping(value = "/sqs-message/{queueUrl}", method = GET)
  public String getMessage(@PathVariable("queueUrl") String queueUrl, Model model) {
    System.out.println(queueUrl);
    List<Message> messages =  sqsService.getSqsMessages("http://localhost:4576/queue/"+queueUrl);
    messages.forEach(System.out::println);
    model.addAttribute("sqsQueue", queueUrl);
    model.addAttribute("messages", messages);
    return "indSqsPage";
  }
}
