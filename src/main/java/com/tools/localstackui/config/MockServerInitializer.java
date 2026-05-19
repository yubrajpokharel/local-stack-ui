package com.tools.localstackui.config;

import com.tools.localstackui.services.MockHttpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MockServerInitializer {

  @Autowired
  private MockHttpService mockHttpService;

  @EventListener(ApplicationReadyEvent.class)
  public void initializeMockServers() {
    try {
      mockHttpService.initializeServersFromEnv();
    } catch (Exception e) {
      System.err.println("Failed to initialize mock servers from environment: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
