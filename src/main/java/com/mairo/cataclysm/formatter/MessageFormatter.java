package com.mairo.cataclysm.formatter;

import org.apache.commons.lang3.StringUtils;

public interface MessageFormatter<T> {

  String LINE_SEPARATOR = System.lineSeparator();
  String DELIMITER = StringUtils.repeat("-", 34) + LINE_SEPARATOR;
  String SUFFIX = "```";
  String PREFIX = SUFFIX + LINE_SEPARATOR;


  String format(T data);

}
