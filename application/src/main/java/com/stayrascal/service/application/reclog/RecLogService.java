package com.stayrascal.service.application.reclog;

import com.stayrascal.service.application.domain.RecLog;

import java.util.List;

public interface RecLogService {
  List<RecLog> getRecLogByUserId(long userId);

  List<RecLog> listRecLogs();

  RecLog addRecLog(RecLog log);
}
