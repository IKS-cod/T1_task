package com.t1.task.exception;

public class KafkaMessageProcessingException extends RuntimeException {
  public KafkaMessageProcessingException(String message) {
    super(message);
  }

  public KafkaMessageProcessingException(String message, Throwable cause) {
    super(message, cause);
  }
}
