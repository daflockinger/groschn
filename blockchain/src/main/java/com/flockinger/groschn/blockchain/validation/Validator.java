package com.flockinger.groschn.blockchain.validation;

public interface Validator<T> {
  Assessment validate(T value);
}
