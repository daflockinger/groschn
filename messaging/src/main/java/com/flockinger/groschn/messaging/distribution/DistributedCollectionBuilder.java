package com.flockinger.groschn.messaging.distribution;

public interface DistributedCollectionBuilder {

  <E> DistributedExternalSet<E> createSetWithListener(SetEventListener<E> listener, String collectionName);
}
