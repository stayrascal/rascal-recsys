package com.stayrascal.service.application.reclog;

import com.stayrascal.service.application.domain.RecLog;
import com.stayrascal.service.application.repository.RecLogRepository;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class RecLogServiceImpl implements RecLogService {
  @Autowired
  private RecLogRepository repository;

  @Override
  public List<RecLog> getRecLogByUserId(long userId) {
    try {
      return repository.getRecLogByUserId(userId);
    } catch (DataAccessException e) {
      throw new RecLogException("Failed to query recommendation history for user: " + userId, e);
    }
  }

  @Override
  public List<RecLog> listRecLogs() {
    try {
      return repository.listRecLogs();
    } catch (DataAccessException e) {
      throw new RecLogException("Failed to query recommendation histories.", e);
    }
  }

  @Override
  public RecLog addRecLog(RecLog log) {
    try {
      log.setCreateTime(new Date(System.currentTimeMillis()));
      repository.addRecLog(log);
      return log;
    } catch (DataAccessException e) {
      throw new RecLogException("Failed to add recommendation history for user: " + log.getUserId(), e);
    }
  }
}
