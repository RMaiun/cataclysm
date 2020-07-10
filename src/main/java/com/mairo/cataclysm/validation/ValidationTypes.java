package com.mairo.cataclysm.validation;

import com.mairo.cataclysm.dto.AddRoundDto;
import com.mairo.cataclysm.dto.FindLastRoundsDto;

import static com.mairo.cataclysm.validation.NumberValidationFunctions.intBetween;
import static com.mairo.cataclysm.validation.NumberValidationFunctions.longBetween;
import static com.mairo.cataclysm.validation.StringValidationFunctions.isSeason;
import static com.mairo.cataclysm.validation.ValidationRule.requiredRule;
import static com.mairo.cataclysm.validation.ValidationSchema.schema;

public interface ValidationTypes {

  ValidationType<FindLastRoundsDto> listLastRoundsValidationType = dto ->
      schema()
          .witRule(requiredRule(dto.getSeason(), "season", isSeason()))
          .witRule(requiredRule(dto.getQty(), "qty", intBetween(1, 1000)));

  ValidationType<AddRoundDto> addRoundValidationType = dto ->
      schema()
          .witRule(requiredRule(dto.getW1(), "w1", longBetween(1L, Long.MAX_VALUE)))
          .witRule(requiredRule(dto.getW2(), "w2", longBetween(1L, Long.MAX_VALUE)))
          .witRule(requiredRule(dto.getL1(), "l1", longBetween(1L, Long.MAX_VALUE)))
          .witRule(requiredRule(dto.getL2(), "l2", longBetween(1L, Long.MAX_VALUE)));

  ValidationType<String> seasonValidationType = dto ->
      schema().witRule(requiredRule(dto, "season", isSeason()));
}