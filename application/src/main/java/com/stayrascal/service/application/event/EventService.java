package com.stayrascal.service.application.event;

import com.stayrascal.service.application.domain.Event;

import java.util.List;

public interface EventService {
  Event addEvent(Event event);

  List<Event> listEvents();

  List<Event> getEventsByUserId(long userId);
}
