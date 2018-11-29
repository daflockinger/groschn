package com.flockinger.groschn.messaging.util;

import com.flockinger.groschn.messaging.exception.ReceivedMessageInvalidException;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.springframework.stereotype.Component;

@Component
public class BeanValidator {

  private final Validator validator;

  public BeanValidator() {
    final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  public <T> void assertEntity(T entity) {
    var possibleErrors = validator.validate(entity);
    if(!possibleErrors.isEmpty()) {
      var errorMessage = possibleErrors.stream()
          .map(this::errorToString).collect(Collectors.joining("\n"));
      throw new ReceivedMessageInvalidException(errorMessage);
    }
  }

  private <T> String errorToString(ConstraintViolation<T> error) {
    StringBuilder errorMessage = new StringBuilder();
    errorMessage.append(error.getPropertyPath() + " " + error.getMessage());

    if(error.getInvalidValue() != null) {
      errorMessage.append(" but was: " + error.getInvalidValue());
    }
    errorMessage.append("\n");
    return errorMessage.toString();
  }
}
