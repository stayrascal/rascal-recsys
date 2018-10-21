package com.stayrascal.service.application.repository;

import com.stayrascal.service.application.domain.QueryLog;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QueryLogRepository {
  List<QueryLog> getQueryLogByUserId(@Param("userId") long userId);

  void addQueryLog(@Param("log") QueryLog queryLog);
}
