package com.stayrascal.recom.spark

import java.util.regex.Pattern

object MovieYearRegex {
  val moduleType = ".*\\(([1-9][0-9][0-9][0-9])\\).*"

  def movieYearReg(str: String): Int = {
    var retYear = 1994
    val pattern = Pattern.compile(moduleType)
    val matcher = pattern.matcher(str)
    while (matcher.find()) {
      retYear = matcher.group(1).toInt
    }
    retYear
  }
}
