package com.flockinger.groschn.messaging.outbound;

import java.io.Serializable;
import java.util.List;
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
   * Sends a message to a list of pre defined nodes (by nodeId).
   * 
   * @param message
   * @param receiverNodeIds
   * @param topic
   */
  void multicast(Message<T> message, List<String> receiverNodeIds, MainTopics topic);
  
  
  /**
   * Sends a message to one specified node (by nodeId).
   * 
   * @param message
   * @param receiverNodeId
   * @param topic
   */
  void unicast(Message<T> message, String receiverNodeId, MainTopics topic);
  
  
  
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
