package com.stayrascal.service.application.querylog;

public class QueryLogException extends RuntimeException {
  public QueryLogException(String message) {
    super(message);
  }

  public QueryLogException(String message, Throwable cause) {
    super(message, cause);
  }
}
