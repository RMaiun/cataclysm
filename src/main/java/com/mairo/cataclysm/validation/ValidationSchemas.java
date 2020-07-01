package com.mairo.cataclysm.validation;

import static com.mairo.cataclysm.validation.NumberValidationFunctions.intBetween;
import static com.mairo.cataclysm.validation.NumberValidationFunctions.longBetween;
import static com.mairo.cataclysm.validation.StringValidationFunctions.isSeason;
import static com.mairo.cataclysm.validation.ValidationRule.requiredRule;
import static com.mairo.cataclysm.validation.ValidationSchema.schema;

import com.mairo.cataclysm.dto.AddRoundDto;
import com.mairo.cataclysm.dto.FindLastRoundsDto;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public interface ValidationSchemas {

  Function<FindLastRoundsDto, ValidationSchema> listLastRoundsValidationSchema = dto ->
      schema(
          requiredRule(dto.getSeason(), "season", isSeason()),
          requiredRule(dto.getQty(), "qty", intBetween(1, 1000))
      );

  Function<AddRoundDto, ValidationSchema> addRoundValidationSchema = dto ->
      schema(
          requiredRule(dto.getW1(), "w1", longBetween(1L, Long.MAX_VALUE)),
          requiredRule(dto.getW2(), "w2", longBetween(1L, Long.MAX_VALUE)),
          requiredRule(dto.getL1(), "l1", longBetween(1L, Long.MAX_VALUE)),
          requiredRule(dto.getL2(), "l2", longBetween(1L, Long.MAX_VALUE))
      );
}
