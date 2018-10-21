package com.stayrascal.service.application.querylog;

import com.stayrascal.service.application.domain.QueryLog;

import java.util.List;

public interface QueryLogService {
  List<QueryLog> getQueryLogByUserId(long userId);

  QueryLog addQueryLog(QueryLog log);
}
