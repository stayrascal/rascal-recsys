package com.stayrascal.service.application.item;

public class ItemLoadException extends ItemException {
  public ItemLoadException(String message) {
    super(message);
  }

  public ItemLoadException(String message, Throwable cause) {
    super(message, cause);
  }
}
