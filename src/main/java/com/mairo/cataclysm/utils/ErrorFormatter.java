package com.mairo.cataclysm.utils;

import com.mairo.cataclysm.formatter.MessageFormatter;

public class ErrorFormatter {

  private ErrorFormatter() {

  }

  public static String format(Throwable error) {
    return String.format("%sERROR: %s%s", MessageFormatter.PREFIX, error.getMessage(), MessageFormatter.SUFFIX);
  }
}
