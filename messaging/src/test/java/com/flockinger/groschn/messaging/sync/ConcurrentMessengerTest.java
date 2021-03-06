package com.flockinger.groschn.messaging.sync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import org.junit.Test;

public class ConcurrentMessengerTest {

  @Test(timeout=1000)
  public void testFetch_withAllItemsFast_shouldReturnThem() {
    ConcurrentMessenger messenger = new ConcurrentMessenger();
    var messages = ImmutableList.of("a", "b", "c", "d", "e", "f");

    var results = messenger.fetch(messages, it ->  CompletableFuture.supplyAsync(() -> {
        try {
          Thread.sleep(20);
        } catch (Exception e) {}
      return it;
    }));
    
    assertTrue("verify that it contains at least the wanted amount of items", results.size() >= 3);
  }
  
  @Test(timeout=1000)
  public void testFetch_withTwoItemsFailing_shouldReturnFastOnes() {
    ConcurrentMessenger messenger = new ConcurrentMessenger();
    messenger = new ConcurrentMessenger();
    
    var messages = ImmutableList.of("a", "b", "c", "d", "e", "f");

    var results = messenger.fetch(messages, it -> CompletableFuture.supplyAsync(() -> {
      if(it.equals("b") || it.equals("d")) {
        throw new RuntimeException();
      }
      return it;
    }));
    
    assertTrue("verify that it contains at least the wanted amount of items", results.size() >= 3);
    assertTrue("verify that it conains the fast items", ImmutableList.of("a", "c","e", "f").containsAll(results));
  }
  
  

  @Test
  public void testFetch_withTooMuchItemsFailing_shouldReturnFastOnes() {
    ConcurrentMessenger messenger = new ConcurrentMessenger();
    var messages = ImmutableList.of("a", "b", "c", "d", "e", "f");

    var results = messenger.fetch(messages, it ->  CompletableFuture.supplyAsync(() -> {
      if(it.equals("b") || it.equals("d") || it.equals("a") || it.equals("f")) {
          throw new NullPointerException();
      }
      return it;
    }));
    
    assertEquals("verify that it contains the fast amount of items", 2, results.size());
    assertTrue("verify that it conains the fast items", results.containsAll(ImmutableList.of("e", "c")));
  }
  
  @Test
  public void testFetch_withAllFailing_shouldReturnEmpty() {
    ConcurrentMessenger messenger = new ConcurrentMessenger();
    var messages = ImmutableList.of("a", "b", "c", "d", "e", "f");

    var results = messenger.fetch(messages, it ->  CompletableFuture.supplyAsync(() -> {
          throw new RuntimeException();
    }));
    
    assertTrue("verify that it returns empty", results.isEmpty());
  }
  
  @Test
  public void testFetch_withAllFailingWithDifferentException_shouldReturnEmpty() {
    ConcurrentMessenger messenger = new ConcurrentMessenger();
    var messages = ImmutableList.of("a", "b", "c", "d", "e", "f");

    var results = messenger.fetch(messages, it ->  CompletableFuture.supplyAsync(() -> {
          throw new NullPointerException();
    }));
    
    assertTrue("verify that it returns empty", results.isEmpty());
  }

  @Test
  public void testFetch_withEmptyRequests_shouldReturnEmpty() {
    ConcurrentMessenger messenger = new ConcurrentMessenger();

    var results = messenger.fetch(new ArrayList<>(), it ->  CompletableFuture.supplyAsync(() -> {
      throw new NullPointerException();
    }));

    assertTrue("verify that it returns empty", results.isEmpty());
  }
}
