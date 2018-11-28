package com.flockinger.groschn.messaging.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
class ConcurrentMessenger {
  
  private final static Logger LOG = LoggerFactory.getLogger(ConcurrentMessenger.class);

  public <R,M> List<M> fetch(List<R> requests, Function<R, CompletableFuture<M>> caller) {
    var minMessageReceiveCount = Math.max(3, requests.size() / 2);

    if(requests.isEmpty()) {
      return new ArrayList<>();
    }
    return Flux.fromIterable(requests)
        .parallel(requests.size())
        .runOn(Schedulers.parallel())
        .flatMap(it -> Mono.fromFuture(caller.apply(it)) )
        .sequential()
        .onErrorContinue(RuntimeException.class, (e,f) -> LOG.trace("Message request failed!",e))
        .take(minMessageReceiveCount)
        .collectList()
        .block();  
  }
  
}
