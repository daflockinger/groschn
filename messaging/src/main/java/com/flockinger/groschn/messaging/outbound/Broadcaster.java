package com.flockinger.groschn.messaging.outbound;

import java.io.Serializable;
import java.util.List;
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
   */
  void broadcast(Message<T> message);
  
  
  /**
   * Sends a message to a list of pre defined nodes (by nodeId).
   * 
   * @param message
   * @param receiverNodeIds
   */
  void multicast(Message<T> message, List<String> receiverNodeIds);
  
  
  /**
   * Sends a message to one specified node (by nodeId).
   * 
   * @param message
   * @param receiverNodeId
   */
  void unicast(Message<T> message, String receiverNodeId);
  
  
  
  /**
   * Sends a request/response message and waits for a response of the receiver.
   * 
   * @param request Message
   * @param receiverNodeId ID of the receiver
   * @return response Message
   */
  Message<T> sendRequest(Message<T> request, String receiverNodeId);
}
