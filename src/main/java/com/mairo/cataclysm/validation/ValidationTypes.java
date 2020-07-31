package com.mairo.cataclysm.validation;

import static com.mairo.cataclysm.validation.NumberValidationFunctions.intBetween;
import static com.mairo.cataclysm.validation.NumberValidationFunctions.longBetween;
import static com.mairo.cataclysm.validation.StringValidationFunctions.containsOnlyLetters;
import static com.mairo.cataclysm.validation.StringValidationFunctions.isLong;
import static com.mairo.cataclysm.validation.StringValidationFunctions.isSeason;
import static com.mairo.cataclysm.validation.StringValidationFunctions.length;
import static com.mairo.cataclysm.validation.ValidationRule.requiredRule;
import static com.mairo.cataclysm.validation.ValidationSchema.schema;

import com.mairo.cataclysm.dto.AddPlayerDto;
import com.mairo.cataclysm.dto.AddRoundDto;
import com.mairo.cataclysm.dto.FindLastRoundsDto;

public interface ValidationTypes {

  ValidationType<FindLastRoundsDto> listLastRoundsValidationType = dto ->
      schema()
          .withRule(requiredRule(dto.getSeason(), "season", isSeason()))
          .withRule(requiredRule(dto.getQty(), "qty", intBetween(1, 1000)));

  ValidationType<AddRoundDto> addRoundValidationType = dto ->
      schema()
          .withRule(requiredRule(dto.getW1(), "w1", longBetween(1L, Long.MAX_VALUE)))
          .withRule(requiredRule(dto.getW2(), "w2", longBetween(1L, Long.MAX_VALUE)))
          .withRule(requiredRule(dto.getL1(), "l1", longBetween(1L, Long.MAX_VALUE)))
          .withRule(requiredRule(dto.getL2(), "l2", longBetween(1L, Long.MAX_VALUE)));

  ValidationType<String> seasonValidationType = dto ->
      schema().withRule(requiredRule(dto, "season", isSeason()));

  ValidationType<AddPlayerDto> addPlayerValidationType = dto ->
      schema()
          .withRule(requiredRule(dto.getId(), "id", isLong()))
          .withRule(requiredRule(dto.getSurname(), "surname", length(2, 20), containsOnlyLetters()));
}
