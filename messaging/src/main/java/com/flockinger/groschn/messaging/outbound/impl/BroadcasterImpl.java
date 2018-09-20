package com.flockinger.groschn.messaging.outbound.impl;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
  
  private Function<Message<MessagePayload>, byte[]> encoder = null;
  private Function<byte[], Message<MessagePayload>> decoder = null;
  
  @Override
  public void broadcast(Message<MessagePayload> message, MainTopics topic) {
    clusterCommunicator().broadcast(topic.name(), message, encoder);
  }

  @Override
  public void multicast(Message<MessagePayload> message, List<String> receiverNodeIds,
      MainTopics topic) {
    var recepients = receiverNodeIds.stream().map(MemberId::from).collect(Collectors.toSet());
    clusterCommunicator().multicast(topic.name(), message, encoder, recepients);
  }

  @Override
  public void unicast(Message<MessagePayload> message, String receiverNodeId, MainTopics topic) {
    clusterCommunicator().unicast(topic.name(), message, encoder, MemberId.from(receiverNodeId));
  }

  @Override
  public CompletableFuture<Message<MessagePayload>> sendRequest(Message<MessagePayload> request,
      String receiverNodeId, MainTopics topic) {
    Member member = atomix.getMembershipService().getMember(receiverNodeId);
    byte[] requestBytes = encoder.apply(request);
    var timeout = Duration.ofSeconds(responseTimeoutSeconds);
    return atomix.getMessagingService()
        .sendAndReceive(member.address(), topic.name(), requestBytes, timeout, pooledExecutor)
        .thenApply(decoder::apply);
  }
  
  private ClusterCommunicationService clusterCommunicator() {
    return atomix.getCommunicationService();
  }

  public void setEncoder(Function<Message<MessagePayload>, byte[]> encoder) {
    this.encoder = encoder;
  }
  public void setDecoder(Function<byte[], Message<MessagePayload>> decoder) {
    this.decoder = decoder;
  }
}
