package com.mairo.cataclysm.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class DateUtils {

  private DateUtils() {
  }

  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
  private static final ZoneId EET_ZONE = ZoneId.of("Europe/Kiev");

  public static LocalDateTime now() {
    return LocalDateTime.now(ZoneOffset.UTC);
  }

  public static LocalDateTime utcToEet(LocalDateTime utcDateTime) {
    ZonedDateTime utcTimeZoned = ZonedDateTime.of(utcDateTime, ZoneId.of("UTC"));
    return utcTimeZoned.withZoneSameInstant(EET_ZONE).toLocalDateTime();
  }

  public static String formatDateWithHour(LocalDateTime utcDateTime) {
    LocalDateTime date = utcToEet(utcDateTime);
    String month = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
    int day = date.getDayOfMonth();
    String dateTime = date.format(formatter);
    int year = date.getYear();
    return String.format("%s %s %s %s", dateTime, day, month, year);
  }
}
