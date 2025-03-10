package com.t1.task.exception;

public class KafkaMessageSendingException extends RuntimeException {
  public KafkaMessageSendingException(String message) {
    super(message);
  }

  public KafkaMessageSendingException(String message, Throwable cause) {
    super(message, cause);
  }
}
