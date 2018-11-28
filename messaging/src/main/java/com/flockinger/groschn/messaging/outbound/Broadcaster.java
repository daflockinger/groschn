package com.flockinger.groschn.messaging.outbound;

import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.RequestParams;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Responsible of sending messages through the node-network.
 */
public interface Broadcaster {

  /**
   * Sends a message to all other connected nodes.
   */
  <R extends Hashable<R>> void broadcast(R uncompressedEntity, String senderId, MainTopics topic);


  /**
   * Sends a request/response message and waits for a response of the receiver.
   */
  <M extends Hashable<M>> CompletableFuture<Optional<M>> sendRequest(RequestParams requestParams,  Class<M> responseType);
}
