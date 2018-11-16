package com.flockinger.groschn.messaging.outbound.impl;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.commons.serialize.BlockSerializer;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.MessageResponder;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.outbound.Broadcaster;
import com.flockinger.groschn.messaging.util.MessagingUtils;
import io.atomix.cluster.MemberId;
import io.atomix.cluster.messaging.ClusterCommunicationService;

@Service
public class BroadcasterImpl implements Broadcaster<MessagePayload> {

  private final static Logger LOG = LoggerFactory.getLogger(BroadcasterImpl.class);
  
  @Autowired
  private ClusterCommunicationService clusterCommunicationService;
  @Value("${blockchain.messaging.response-timeout}")
  private Integer responseTimeoutSeconds;
  @Autowired
  private BlockSerializer serializer;
  @Autowired
  private Executor pooledExecutor;
  @Autowired
  private List<MessageResponder<MessagePayload>> responders;
  @Autowired
  private MessagingUtils utils;
  
  @PostConstruct
  public void init() {
    for(MessageResponder<MessagePayload> responder: responders) {
      clusterCommunicationService.subscribe(responder.getSubscribedTopic().name(), 
          decoder(), responder::respond, serializer::serialize, pooledExecutor);
      LOG.info("Registered message responder for topic {}", responder.getSubscribedTopic().name());
    }
  }
  
  @Override
  public <T extends Hashable<T>> void broadcast(T uncompressedEntity, String senderId, MainTopics topic) {
    clusterCommunicationService.broadcast(topic.name(), utils.packageMessage(uncompressedEntity, senderId), serializer::serialize);
  }

  @Override
  @SuppressWarnings("unchecked")
  public CompletableFuture<Message<MessagePayload>> sendRequest(Message<MessagePayload> request,
      String receiverNodeId, MainTopics topic) {
    return clusterCommunicationService.send(topic.name(), request, serializer::serialize, decoder(), 
        MemberId.from(receiverNodeId), Duration.ofSeconds(responseTimeoutSeconds));
  }
  
  @SuppressWarnings("unchecked")
  private Function<byte[], Message<MessagePayload>> decoder() {
    return payload -> (Message<MessagePayload>)serializer.deserialize(payload, Message.class);
  }
}
