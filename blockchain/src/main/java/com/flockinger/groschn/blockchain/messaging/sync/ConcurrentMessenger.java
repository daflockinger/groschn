package com.flockinger.groschn.blockchain.messaging.sync;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.datatype.threetenbp.function.Function;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class ConcurrentMessenger {

  public <R,M> List<M> fetch(List<R> requests, Function<R, CompletableFuture<M>> caller) {
    var minMessageReceiveCount = Math.max(3, requests.size() / 2);
    
    return Flux.fromIterable(requests)
        .parallel(requests.size())
        .runOn(Schedulers.parallel())
        .flatMap(it -> Mono.fromFuture(caller.apply(it)) )
        .sequential()
        .onErrorContinue(RuntimeException.class, (e,f) -> {})
        .take(minMessageReceiveCount)
        .collectList()
        .block();  
  }
  
}
