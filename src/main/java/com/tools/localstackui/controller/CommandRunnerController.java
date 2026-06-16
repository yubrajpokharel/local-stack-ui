package com.tools.localstackui.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.tools.localstackui.services.CommandRunnerService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommandRunnerController {

  @Autowired
  CommandRunnerService commandRunnerService;

  @GetMapping(value = "/command-runner/directories", produces = APPLICATION_JSON_VALUE)
  public List<String> getDirectories() {
    return commandRunnerService.getAllowedDirectories();
  }

  @PostMapping(value = "/command-runner/run", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> runCommand(@RequestParam String cwd,
      @RequestParam String command) {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.putAll(commandRunnerService.runCommand(cwd, command));
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }
}
