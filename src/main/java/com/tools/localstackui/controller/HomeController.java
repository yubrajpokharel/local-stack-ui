package com.tools.localstackui.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.amazonaws.services.sns.AmazonSNS;
import com.tools.localstackui.services.SNSService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HomeController {

  @Autowired
  SNSService snsService;

@RequestMapping(value = "/home", method = RequestMethod.GET)
public String home(Model model){
    return "index";
  }

  @GetMapping(value = "/sns-topics/{topicArn}", produces = APPLICATION_JSON_VALUE)
  public String getIndividualTopic(@PathVariable("topicArn") String topicArn, Model model) {
    List<String> subscriptions =  snsService.getSubscriptionByTopicName(topicArn);
    model.addAttribute("snsTopic", topicArn);
    model.addAttribute("subscriptions", subscriptions);
    return "indTopicPage";
  }

}
