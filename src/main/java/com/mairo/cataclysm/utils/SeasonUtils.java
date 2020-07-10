package com.mairo.cataclysm.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class SeasonUtils {
  private SeasonUtils() {
  }

  public static String currentSeason() {
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
    int month = now.getMonth().getValue();
    int year = now.getYear();
    int q = month <= 3 ? 1 : month <= 6 ? 2 : month <= 9 ? 3 : 4;
    return String.format("S%d|%d", q, year);
  }
}
