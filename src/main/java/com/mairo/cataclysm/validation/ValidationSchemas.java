package com.mairo.cataclysm.validation;

import static com.mairo.cataclysm.validation.NumberValidationFunctions.intBetween;
import static com.mairo.cataclysm.validation.NumberValidationFunctions.longBetween;
import static com.mairo.cataclysm.validation.StringValidationFunctions.isSeason;
import static com.mairo.cataclysm.validation.ValidationRule.requiredRule;
import static com.mairo.cataclysm.validation.ValidationSchema.schema;
import static java.util.List.of;

import com.mairo.cataclysm.dto.AddRoundDto;
import com.mairo.cataclysm.dto.FindLastRoundsDto;
import java.util.function.Function;

public interface ValidationSchemas {

  Function<FindLastRoundsDto, ValidationSchema> listLastRoundsValidationSchema = dto ->
      schema()
          .witRule(requiredRule(dto.getSeason(), "season", of(isSeason())))
          .witRule(requiredRule(dto.getQty(), "qty", of(intBetween(1, 1000))));

  Function<AddRoundDto, ValidationSchema> addRoundValidationSchema = dto ->
      schema()
          .witRule(requiredRule(dto.getW1(), "w1", of(longBetween(1L, Long.MAX_VALUE))))
          .witRule(requiredRule(dto.getW2(), "w2", of(longBetween(1L, Long.MAX_VALUE))))
          .witRule(requiredRule(dto.getL1(), "l1", of(longBetween(1L, Long.MAX_VALUE))))
          .witRule(requiredRule(dto.getL2(), "l2", of(longBetween(1L, Long.MAX_VALUE))));
}
