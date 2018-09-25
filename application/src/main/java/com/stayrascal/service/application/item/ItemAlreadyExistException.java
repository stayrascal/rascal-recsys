package com.stayrascal.service.application.item;

public class ItemAlreadyExistException extends ItemException {
  public ItemAlreadyExistException(String message) {
    super(message);
  }

  public ItemAlreadyExistException(String message, Throwable cause) {
    super(message, cause);
  }
}
