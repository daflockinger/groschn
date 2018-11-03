package com.flockinger.groschn.messaging.inbound;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Function;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.commons.serialize.BlockSerializer;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import io.atomix.cluster.messaging.ClusterCommunicationService;

@Component
public class MessageDispatcher {

  @Autowired
  private List<MessageListener<MessagePayload>> listeners;
  
  @Autowired
  private BlockSerializer serializer;
  
  @Autowired
  private ClusterCommunicationService clusterCommunicationService;
  @Autowired
  private Executor pooledExecutor;
  
  private final static Logger LOG = LoggerFactory.getLogger(MessageDispatcher.class);
  
  @PostConstruct
  public void init() {
    for(MessageListener<MessagePayload> listener: listeners) {
      clusterCommunicationService.subscribe(listener.getSubscribedTopic().name(), 
          decoder(), listener::receiveMessage, pooledExecutor);
      LOG.info("Registered message listener for topic {}", listener.getSubscribedTopic().name());
    }
  }
  
  @SuppressWarnings("unchecked")
  public Function<byte[], Message<MessagePayload>> decoder() {
    return payload -> (Message<MessagePayload>)serializer.deserialize(payload, Message.class);
  }
}
