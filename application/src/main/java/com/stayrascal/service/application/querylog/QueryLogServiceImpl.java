package com.stayrascal.service.application.querylog;

import com.stayrascal.service.application.domain.QueryLog;
import com.stayrascal.service.application.repository.QueryLogRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class QueryLogServiceImpl implements QueryLogService {
  @Autowired
  private QueryLogRepository repository;

  @Override
  public List<QueryLog> getQueryLogByUserId(long userId) {
    try {
      return repository.getQueryLogByUserId(userId);
    } catch (DataAccessException e) {
      throw new QueryLogException("Failed to query events for user: " + userId, e);
    }
  }

  @Override
  public QueryLog addQueryLog(QueryLog log) {
    try {
      repository.addQueryLog(log);
      return log;
    } catch (DataAccessException e) {
      throw new QueryLogException("Failed to add events for user: " + log.getUserId(), e);
    }
  }
}
