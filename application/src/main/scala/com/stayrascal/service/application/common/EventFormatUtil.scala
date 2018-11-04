package com.stayrascal.service.application.common

import java.util.regex.Pattern

object EventFormatUtil {
  private val historyPattern: Pattern = Pattern.compile("\\w+,\\w+\\.\\w+,\\w+\\.\\w+")
  private val eventPattern: Pattern = Pattern.compile("\\w+,\\w+\\.\\w+,\\w+\\.\\w+")

  def isValidateHistory(recordStr: String): Boolean = {
    historyPattern.matcher(recordStr).matches()
  }

  def isValidEvent(eventStr: String): Boolean = {
    eventPattern.matcher(eventStr).matches()
  }
}
