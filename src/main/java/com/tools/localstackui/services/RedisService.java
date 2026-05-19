package com.tools.localstackui.services;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

  @Autowired
  StringRedisTemplate redisTemplate;

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
