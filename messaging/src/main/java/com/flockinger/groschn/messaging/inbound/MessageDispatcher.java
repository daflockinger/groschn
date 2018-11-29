package com.flockinger.groschn.messaging.inbound;

import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.util.MessagingContext;
import java.util.List;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MessageDispatcher {

  private final static Logger LOG = LoggerFactory.getLogger(MessageDispatcher.class);

  public MessageDispatcher(List<MessageListener<MessagePayload>> listeners, MessagingContext utils) {
    var clusterCommunicationService = utils.clusterCommunicationService();

    for(MessageListener<MessagePayload> listener: listeners) {
      clusterCommunicationService.subscribe(listener.getSubscribedTopic().name(),
          decoder(utils), listener::receiveMessage, utils.executor());
      LOG.info("Registered message listener for topic {}", listener.getSubscribedTopic().name());
    }
  }

  @SuppressWarnings("unchecked")
  public Function<byte[], Message<MessagePayload>> decoder(MessagingContext utils) {
    return payload -> (Message<MessagePayload>) utils.serializer().deserialize(payload, Message.class);
  }
}
