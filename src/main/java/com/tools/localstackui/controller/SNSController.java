package com.tools.localstackui.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.tools.localstackui.services.SNSService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class SNSController {

  @Autowired
  SNSService snsService;

  @GetMapping(value = "/sns-topics/{topicArn}", produces = APPLICATION_JSON_VALUE)
  public String getIndividualTopic(@PathVariable("topicArn") String topicArn, Model model) {
    Map<String, String> subscriptions =  snsService.getSubscriptionByTopicName(topicArn);
    model.addAttribute("snsTopic", topicArn);
    model.addAttribute("subscriptions", subscriptions);
    return "indTopicPage";
  }

}
