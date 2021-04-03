package com.mairo.cataclysm.utils;

import org.apache.commons.lang3.StringUtils;

public interface Constants {

  String LINE_SEPARATOR = System.lineSeparator();
  String DELIMITER = StringUtils.repeat("-", 34) + LINE_SEPARATOR;
  String SUFFIX = "```";
  String PREFIX = SUFFIX + LINE_SEPARATOR;
}
