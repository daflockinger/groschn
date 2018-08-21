package com.flockinger.groschn.messaging.distribution;

import java.util.Set;

public interface DistributedExternalSet<E> {

  Set<E> getStaticSet();
  Long size();
}
