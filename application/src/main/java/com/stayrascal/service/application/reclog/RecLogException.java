package com.stayrascal.service.application.reclog;

public class RecLogException extends RuntimeException {
  public RecLogException(String message) {
    super(message);
  }

  public RecLogException(String message, Throwable cause) {
    super(message, cause);
  }
}
