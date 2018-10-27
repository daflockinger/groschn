package com.flockinger.groschn.messaging.outbound;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.Message;

/**
 * Responsible of sending messages through the node-network.
 *
 * @param <T>
 */
public interface Broadcaster<T extends Serializable> {

  /**
   * Sends a message to all other connected nodes.
   * 
   * @param message
   * @param topic
   */
  void broadcast(Message<T> message, MainTopics topic);
  
  
  /**
   * Sends a request/response message and waits for a response of the receiver.
   * 
   * @param request Message
   * @param receiverNodeId ID of the receiver
   * @param topic
   * @return response Message
   */
  CompletableFuture<Message<T>> sendRequest(Message<T> request, String receiverNodeId, MainTopics topic);
}
