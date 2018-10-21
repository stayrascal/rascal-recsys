package com.stayrascal.service.application.repository;

import com.stayrascal.service.application.domain.Event;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository {
  List<Event> listAllEvent();

  List<Event> getEventsByUserId(@Param("userId") long userId);

  void addEvent(@Param("event") Event event);
}
