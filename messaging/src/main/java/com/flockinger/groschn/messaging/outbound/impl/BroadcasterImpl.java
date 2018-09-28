package com.flockinger.groschn.messaging.outbound.impl;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.commons.serialize.BlockSerializer;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.outbound.Broadcaster;
import io.atomix.cluster.Member;
import io.atomix.cluster.MemberId;
import io.atomix.cluster.messaging.ClusterCommunicationService;
import io.atomix.core.Atomix;

@Service
public class BroadcasterImpl implements Broadcaster<MessagePayload> {

  @Autowired
  private Atomix atomix;
  @Autowired
  private Executor pooledExecutor;
  
  @Value("${blockchain.messaging.response-timeout}")
  private Integer responseTimeoutSeconds;
  
  @Autowired
  private BlockSerializer serializer;
  
  @Override
  public void broadcast(Message<MessagePayload> message, MainTopics topic) {
    clusterCommunicator().broadcast(topic.name(), message, serializer::serialize);
  }

  @Override
  @SuppressWarnings("unchecked")
  public CompletableFuture<Message<MessagePayload>> sendRequest(Message<MessagePayload> request,
      String receiverNodeId, MainTopics topic) {
    Member member = atomix.getMembershipService().getMember(receiverNodeId);
    byte[] requestBytes = serializer.serialize(request);
    var timeout = Duration.ofSeconds(responseTimeoutSeconds);
    return atomix.getMessagingService()
        .sendAndReceive(member.address(), topic.name(), requestBytes, timeout, pooledExecutor)
        .thenApply(message -> (Message<MessagePayload>)serializer.deserialize(message, Message.class));
  }
  
  private ClusterCommunicationService clusterCommunicator() {
    return atomix.getCommunicationService();
  }
}
