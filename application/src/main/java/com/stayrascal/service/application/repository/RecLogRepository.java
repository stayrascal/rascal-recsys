package com.stayrascal.service.application.repository;

import com.stayrascal.service.application.domain.RecLog;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecLogRepository {
  List<RecLog> getRecLogByUserId(@Param("userId") long userId);

  List<RecLog> listRecLogs();

  void addRecLog(@Param("log") RecLog log);

}
