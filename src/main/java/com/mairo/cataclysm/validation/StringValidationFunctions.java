package com.mairo.cataclysm.validation;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public interface StringValidationFunctions {

  static ValidationFunction<String> length(int start, int end) {
    return vf -> (vf.getData().length() >= start && vf.getData().length() <= end)
        ? emptyList()
        : singletonList(String.format("Expected String length [%d,%d] for prop: %s", start, end, vf.getField()));
  }

  static ValidationFunction<String> oneOf(String... args) {
    return vf -> Arrays.asList(args).contains(vf.getData())
        ? emptyList()
        : singletonList(String.format("Expected one of %s for props: %s", Arrays.toString(args), vf.getField()));
  }

  static ValidationFunction<String> isSeason() {
    String seasonPattern = "^[Ss][1-4]\\|\\d{4}$";
    return vf -> vf.getData().length() == 7 && vf.getData().matches(seasonPattern)
        ? emptyList()
        : singletonList(String.format("Field %s is not a season (Season pattern:S{1-4}|yyyy)", vf.getField()));
  }

  static ValidationFunction<String> containsOnlyLetters() {
    return vf -> vf.getData().chars().allMatch(Character::isLetter)
        ? emptyList()
        : singletonList(String.format("Field %s must contain only letters", vf.getField()));
  }

  static ValidationFunction<String> isLong() {
    return data -> {
      try{
        Long.parseLong(data.getData());
        return emptyList();
      }catch (NumberFormatException ex){
        return singletonList(String.format("Field %s must be parsable into Long", data.getField()));
      }
    };
  }
}
