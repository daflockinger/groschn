package com.flockinger.groschn.blockchain.messaging;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Test;

public class BlockFullSyncronizerTest {

  
  ExecutorService smallExec = Executors.newFixedThreadPool(12);

  @Test
  public void test_bla() {

    /*var runningSyncRequests = new ArrayList<CompletableFuture<String>>();
    Stopwatch watch = Stopwatch.createStarted();
    runningSyncRequests.addAll(ImmutableList.of(supplyAsync(() -> someTask(15,true),smallExec),
        supplyAsync(() -> someTask(4,false),smallExec), supplyAsync(() -> someTask(3,false),smallExec),
        supplyAsync(() -> someTask(2,true),smallExec), supplyAsync(() -> someTask(1,false),smallExec)));
    
    runningSyncRequests = //runningSyncRequests.stream().map(fut -> fut.);

    var results = new LinkedList<String>();
    int minimumFineCount = 3;

    runningSyncRequests.forEach(present -> present
        .thenApply(results::add)
        .thenAccept(res -> {
          if(results.size() >= minimumFineCount) {
            stopAll(runningSyncRequests);
          }
        }));
    
    runningSyncRequests.stream().map(request -> request.handle((req,exception) -> {
      //LOG.error("Requesting block sync package failed(probably timeout or offline node): ",exception); 
      return req;})).forEach(CompletableFuture::join);
    
    
    try {
    Awaitility.with().atMost(10, TimeUnit.SECONDS).until(() -> runningSyncRequests.stream().allMatch(this::isDoneOrFailed));    
    } catch(Exception e) {}
    
    results.forEach(System.out::println);
    
    System.out.println();*/
  }
  
  private String logError(String message, Throwable t) {
    return message;
  }
  
  
  private void stopAll(ArrayList<CompletableFuture<String>> requests) {
    requests.stream().filter(this::isRunning).forEach(fut -> fut.cancel(true));;
  }
  
  
  private boolean isRunning(CompletableFuture<?> future) {
    return !(future.isDone() || future.isCompletedExceptionally() || future.isCancelled());
  }


  private String someTask(long howLongShouldItTake, boolean fail) {
    try {
      Thread.sleep(howLongShouldItTake * 1000);
      if(fail) {
        throw new RuntimeException();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return "Done!" + " In: " + howLongShouldItTake;
  }
}
