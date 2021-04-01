package com.mairo.cataclysm.utils;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZonedDateTime;
import org.apache.commons.lang3.tuple.Pair;

public class SeasonUtils {

  private SeasonUtils() {
  }

  public static String currentSeason() {
    ZonedDateTime now = DateUtils.now();
    int month = now.getMonth().getValue();
    int year = now.getYear();
    int q = month <= 3 ? 1 : month <= 6 ? 2 : month <= 9 ? 3 : 4;
    return String.format("S%d|%d", q, year);
  }

  public static Pair<LocalDate, LocalDate> seasonGate(String seasonName) {
    String[] seasonData = seasonName.split("\\|");
    Pair<Integer, Integer> qGate = quarterGate(Integer.parseInt(seasonData[0].split("")[1]));
    int year = Integer.parseInt(seasonData[1]);
    LocalDate start = LocalDate.of(year, qGate.getKey(), 1);
    Month lastMonth = Month.of(qGate.getValue());
    LocalDate tmpDate = LocalDate.of(year, lastMonth, 1);
    LocalDate end = LocalDate.of(year, lastMonth, tmpDate.lengthOfMonth());
    return Pair.of(start, end);
  }

  private static Pair<Integer, Integer> quarterGate(int quarter) {
    switch (quarter) {
      case 1:
        return Pair.of(1, 3);
      case 2:
        return Pair.of(4, 6);
      case 3:
        return Pair.of(7, 9);
      case 4:
        return Pair.of(10, 12);
      default:
        return Pair.of(0, 0);
    }
  }
}
