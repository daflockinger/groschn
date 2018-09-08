package com.flockinger.groschn.blockchain.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
public class CacheConfig {
  
  @Value("${blockchain.messaging.id-cache.expire-after-milliseconds}")
  private Long expireAfterMilliseconds;
  @Value("${blockchain.messaging.id-cache.initial-capacity}")
  private Integer initCapacity;
  @Value("${blockchain.messaging.id-cache.max-capacity}")
  private Long maxCapacity;
  
  @Bean("BlockId_Cache")
  public Cache<String, String> getBlockIdCache() {
    return Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofMillis(expireAfterMilliseconds))
        .initialCapacity(initCapacity)
        .maximumSize(maxCapacity)
        .build();
  }
  
  @Bean("TransactionId_Cache")
  public Cache<String, String> getTransactionIdCache() {
    return Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMillis(expireAfterMilliseconds))
        .initialCapacity(initCapacity)
        .maximumSize(maxCapacity)
        .build();
  }
  
}
