package com.mairo.cataclysm.core.validation;

import static com.mairo.cataclysm.core.validation.NumberValidationFunctions.intBetween;
import static com.mairo.cataclysm.core.validation.StringValidationFunctions.isSeason;
import static com.mairo.cataclysm.core.validation.StringValidationFunctions.length;
import static com.mairo.cataclysm.core.validation.StringValidationFunctions.notEmpty;
import static com.mairo.cataclysm.core.validation.StringValidationFunctions.onlyLetters;
import static com.mairo.cataclysm.core.validation.StringValidationFunctions.onlyNumbers;
import static com.mairo.cataclysm.core.validation.ValidationRule.requiredRule;
import static com.mairo.cataclysm.core.validation.ValidationRule.rule;
import static com.mairo.cataclysm.core.validation.ValidationSchema.schema;

import com.mairo.cataclysm.core.dto.AddPlayerDto;
import com.mairo.cataclysm.core.dto.AddRoundDto;
import com.mairo.cataclysm.core.dto.FindLastRoundsDto;
import com.mairo.cataclysm.core.dto.GenerateStatsDocumentDto;
import com.mairo.cataclysm.core.dto.LinkTidDto;
import com.mairo.cataclysm.core.dto.StoreAuditLogDto;
import com.mairo.cataclysm.core.dto.SubscriptionActionDto;

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

  ValidationType<GenerateStatsDocumentDto> generateStatsDocumentValidationType = dto ->
      schema()
          .withRule(requiredRule(dto.getSeason(), "season", isSeason()));

  ValidationType<LinkTidDto> linkTidValidationType = dto ->
      schema()
          .withRule(rule(dto.getModerator(), "moderator", notEmpty(), onlyNumbers()))
          .withRule(rule(dto.getNameToLink(), "nameToLink", length(2, 20), onlyLetters()))
          .withRule(rule(dto.getTid(), "tid", notEmpty(), onlyNumbers()));

  ValidationType<SubscriptionActionDto> subscriptionActionValidationType = dto ->
      schema()
          .withRule(rule(dto.getTid(), "moderator", notEmpty(), onlyNumbers()));

  ValidationType<StoreAuditLogDto> storeAuditLogValidationType = dto ->
      schema()
          .withRule(requiredRule(dto.getMsg(), "msg", notEmpty()));
}
