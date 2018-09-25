package com.stayrascal.service.application.item;

public class ItemNotFoundException extends ItemException {
  public ItemNotFoundException(String message) {
    super(message);
  }

  public ItemNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
