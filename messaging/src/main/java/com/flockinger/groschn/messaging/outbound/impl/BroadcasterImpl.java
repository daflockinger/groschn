package com.flockinger.groschn.messaging.outbound.impl;

import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.MessageResponder;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.model.RequestParams;
import com.flockinger.groschn.messaging.outbound.Broadcaster;
import com.flockinger.groschn.messaging.util.MessagingContext;
import io.atomix.cluster.MemberId;
import io.atomix.cluster.messaging.ClusterCommunicationService;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BroadcasterImpl implements Broadcaster {

  private final static Logger LOG = LoggerFactory.getLogger(BroadcasterImpl.class);

  @Value("${blockchain.messaging.response-timeout}")
  private Integer responseTimeoutSeconds;

  private final ClusterCommunicationService clusterCommunicationService;
  private final MessagingContext utils;

  public BroadcasterImpl(List<MessageResponder<MessagePayload>> responders, MessagingContext utils) {
    this.utils = utils;
    this.clusterCommunicationService = utils.clusterCommunicationService();

    for (MessageResponder<MessagePayload> responder : responders) {
      clusterCommunicationService.subscribe(responder.getSubscribedTopic().name(),
          decoder(), responder::respond, utils.serializer()::serialize, utils.executor());
      LOG.info("Registered message responder for topic {}", responder.getSubscribedTopic().name());
    }
  }

  @Override
  public <T extends Hashable<T>> void broadcast(T uncompressedEntity, String senderId,
      MainTopics topic) {
    clusterCommunicationService
        .broadcast(topic.name(), utils.packageMessage(uncompressedEntity, senderId),
            utils.serializer()::serialize);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <M extends Hashable<M>> CompletableFuture<Optional<M>> sendRequest(
      RequestParams requestParams, Class<M> responseType) {
    return clusterCommunicationService.send(requestParams.getTopic(),
        utils.packageMessage(requestParams.getSyncRequest(), requestParams.getSenderId()),
        utils.serializer()::serialize, decoder(),
        idFrom(requestParams),
        Duration.ofSeconds(responseTimeoutSeconds))
        .thenApply(it -> utils.extractPayload(it, responseType));
  }

  private MemberId idFrom(RequestParams requestParams) {
    return MemberId.from(requestParams.getReceiverNodeId());
  }

  @SuppressWarnings("unchecked")
  private Function<byte[], Message<MessagePayload>> decoder() {
    return payload -> (Message<MessagePayload>)  utils.serializer().deserialize(payload, Message.class);
  }
}
