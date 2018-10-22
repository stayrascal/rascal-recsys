package com.stayrascal.service.application.event;

import com.stayrascal.service.application.domain.Event;
import com.stayrascal.service.application.repository.EventRepository;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class EventServerImpl implements EventService {
  @Autowired
  private EventRepository repository;

  @Override
  public Event addEvent(Event event) {
    try {
      event.setCreateTime(new Date(System.currentTimeMillis()));
      repository.addEvent(event);
      return event;
    } catch (DataAccessException e) {
      throw new EventException("Failed to add event for user: " + event.getUserId(), e);
    }
  }

  @Override
  public List<Event> listEvents() {
    try {
      return repository.listAllEvent();
    } catch (DataAccessException e) {
      throw new EventException("Failed to query events.", e);
    }
  }

  @Override
  public List<Event> getEventsByUserId(long userId) {
    try {
      return repository.getEventsByUserId(userId);
    } catch (DataAccessException e) {
      throw new EventException("Failed to query events for user: " + userId, e);
    }
  }
}
