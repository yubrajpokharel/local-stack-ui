package com.tools.localstackui.services;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

  @Autowired
  StringRedisTemplate redisTemplate;

  @Value("${spring.data.redis.host:localhost}")
  private String redisHost;

  @Value("${spring.data.redis.port:6379}")
  private int redisPort;

  public String ping() {
    return redisTemplate.execute((RedisCallback<String>) connection -> connection.ping());
  }

  public List<String> getKeys() {
    Set<String> keys = redisTemplate.keys("*");
    if (keys == null || keys.isEmpty()) {
      return emptyList();
    }
    return keys.stream().sorted().collect(toList());
  }

  public List<Map<String, String>> getKeyDetails() {
    return getKeys().stream().map(key -> {
      Map<String, String> keyDetails = new LinkedHashMap<>();
      keyDetails.put("name", key);
      keyDetails.put("address", "redis://" + redisHost + ":" + redisPort + "/" + key);
      keyDetails.put("createdOn", "");
      return keyDetails;
    }).toList();
  }

  public String getValue(String key) {
    return redisTemplate.opsForValue().get(key);
  }

  public void setValue(String key, String value) {
    redisTemplate.opsForValue().set(key, value);
  }

  public Boolean delete(String key) {
    return redisTemplate.delete(key);
  }
}
