package com.stayrascal.service.application.dto.result.item;

import com.stayrascal.service.application.domain.Item;
import com.stayrascal.service.application.dto.result.UpdateResult;

import java.util.List;

public class ItemsUpdateResult extends UpdateResult<Item> {
  public ItemsUpdateResult(List<Item> data) {
    super(data);
  }
}
