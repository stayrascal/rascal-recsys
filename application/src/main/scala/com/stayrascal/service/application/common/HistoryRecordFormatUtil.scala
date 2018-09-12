package com.stayrascal.service.application.common

import java.util.regex.Pattern

object HistoryRecordFormatUtil {
  private val pattern: Pattern = Pattern.compile("\\w+,\\w+\\.\\w+,\\w+\\.\\w+")

  def isValidate(recordStr: String): Boolean = {
    pattern.matcher(recordStr).matches()
  }
}
