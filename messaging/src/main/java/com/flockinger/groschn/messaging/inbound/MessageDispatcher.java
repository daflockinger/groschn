package com.flockinger.groschn.messaging.inbound;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import io.atomix.cluster.messaging.ClusterCommunicationService;
import io.atomix.core.Atomix;

@Component
public class MessageDispatcher {

  @Autowired
  private List<MessageListener<MessagePayload>> listeners;
  @Autowired
  private List<MessageResponder<MessagePayload>> responders;
  
  @Autowired
  private Atomix atomix;
  @Autowired
  private Executor pooledExecutor;
  
  @Value("${blockchain.messaging.response-timeout}")
  private Integer responseTimeoutSeconds;
  
  private Function<Optional<Message<MessagePayload>>, byte[]> encoder = null;
  private Function<byte[], Message<MessagePayload>> decoder = null;
  
  @PostConstruct
  public void init() {
    for(MessageListener<MessagePayload> listener: listeners) {
      clusterCommunicator().subscribe(listener.getSubscribedTopic().name(), 
          decoder, listener::receiveMessage, pooledExecutor);
    }
    for(MessageResponder<MessagePayload> responder: responders) {
      clusterCommunicator().subscribe(responder.getSubscribedTopic().name(), 
          decoder, responder::respond, encoder, pooledExecutor);
    }
  }
  
  private ClusterCommunicationService clusterCommunicator() {
    return atomix.getCommunicationService();
  }
  
  public void setEncoder(Function<Optional<Message<MessagePayload>>, byte[]> encoder) {
    this.encoder = encoder;
  }
  public void setDecoder(Function<byte[], Message<MessagePayload>> decoder) {
    this.decoder = decoder;
  }
}
