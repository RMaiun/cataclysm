package com.mairo.cataclysm.validation;

import static com.mairo.cataclysm.validation.NumberValidationFunctions.intBetween;
import static com.mairo.cataclysm.validation.StringValidationFunctions.isSeason;
import static com.mairo.cataclysm.validation.StringValidationFunctions.length;
import static com.mairo.cataclysm.validation.StringValidationFunctions.notEmpty;
import static com.mairo.cataclysm.validation.StringValidationFunctions.onlyLetters;
import static com.mairo.cataclysm.validation.StringValidationFunctions.onlyNumbers;
import static com.mairo.cataclysm.validation.ValidationRule.requiredRule;
import static com.mairo.cataclysm.validation.ValidationRule.rule;
import static com.mairo.cataclysm.validation.ValidationSchema.schema;

import com.mairo.cataclysm.dto.AddPlayerDto;
import com.mairo.cataclysm.dto.AddRoundDto;
import com.mairo.cataclysm.dto.FindLastRoundsDto;

public interface ValidationTypes {

  ValidationType<FindLastRoundsDto> listLastRoundsValidationType = dto ->
      schema()
          .withRule(requiredRule(dto.getSeason(), "season", isSeason()))
          .withRule(requiredRule(dto.getQty(), "qty", intBetween(1, 10_000)));

  ValidationType<AddRoundDto> addRoundValidationType = dto ->
      schema()
          .withRule(requiredRule(dto.getW1(), "w1", notEmpty(), onlyLetters()))
          .withRule(requiredRule(dto.getW2(), "w2", notEmpty(), onlyLetters()))
          .withRule(requiredRule(dto.getL1(), "l1", notEmpty(), onlyLetters()))
          .withRule(requiredRule(dto.getL2(), "l2", notEmpty(), onlyLetters()))
          .withRule(rule(dto.getModerator(), "moderator", notEmpty()));

  ValidationType<String> seasonValidationType = dto ->
      schema().withRule(requiredRule(dto, "season", isSeason()));

  ValidationType<AddPlayerDto> addPlayerValidationType = dto ->
      schema()
          .withRule(rule(dto.getTid(), "moderator", onlyNumbers()))
          .withRule(requiredRule(dto.getSurname(), "surname", length(2, 20), onlyLetters()))
          .withRule(rule(dto.getModerator(), "moderator", notEmpty()));
}
