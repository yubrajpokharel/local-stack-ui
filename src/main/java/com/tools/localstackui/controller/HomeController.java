package com.tools.localstackui.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller()
public class HomeController {

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

}
