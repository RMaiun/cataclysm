package com.mairo.cataclysm.validation;

import static com.mairo.cataclysm.validation.NumberValidationFunctions.intBetween;
import static com.mairo.cataclysm.validation.StringValidationFunctions.length;
import static com.mairo.cataclysm.validation.StringValidationFunctions.oneOf;
import static com.mairo.cataclysm.validation.ValidationRule.requiredRule;
import static com.mairo.cataclysm.validation.ValidationRule.rule;
import static com.mairo.cataclysm.validation.ValidationSchema.schema;
import static com.mairo.cataclysm.validation.ValidationTypes.listLastRoundsValidationType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mairo.cataclysm.data.ValidationTestData;
import com.mairo.cataclysm.dto.FindLastRoundsDto;
import com.mairo.cataclysm.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class ValidatorTest {

  @Test
  void testSuccessfulValidation() {
    FindLastRoundsDto dto = new FindLastRoundsDto("S2|2020", 1000);
    Mono<FindLastRoundsDto> validate = Validator.validate(dto, listLastRoundsValidationType);
    validate.subscribe(result -> assertEquals(dto, result));
  }

  @Test
  void testInvalidSeason() {
    FindLastRoundsDto dto = new FindLastRoundsDto("S22|4", 3004);
    Mono<FindLastRoundsDto> validate = Validator.validate(dto, listLastRoundsValidationType);
    StepVerifier.create(validate)
        .expectError(ValidationException.class)
        .verify();
  }

  @Test
  void testInvalidQty() {
    FindLastRoundsDto dto = new FindLastRoundsDto("S2|2020", 4000);
    Mono<FindLastRoundsDto> validate = Validator.validate(dto, listLastRoundsValidationType);
    StepVerifier.create(validate)
        .expectError(ValidationException.class)
        .verify();

  }

  @Test
  void testInvalidBothValues() {
    FindLastRoundsDto dto = new FindLastRoundsDto("S22|4", 4000);
    Mono<FindLastRoundsDto> validate = Validator.validate(dto, listLastRoundsValidationType);
    StepVerifier.create(validate)
        .expectErrorSatisfies(throwable -> {
          assertTrue(throwable instanceof ValidationException);
          assertNotNull(throwable.getMessage());
          assertEquals(2, throwable.getMessage().split("\\.").length);
        })
        .verify();
  }

  @Test
  void complexValidationTest() {
    ValidationType<ValidationTestData.Cat> catValidationType = c ->
        schema()
            .withRule(rule(c.sound, "sound", oneOf("mew", "pur")))
            .withRule(rule(c.hungryPercentage, "hungryPrecentage", intBetween(0, 100)));

    ValidationType<ValidationTestData.Person> personValidationType = p ->
        schema()
            .withRule(requiredRule(p.age, "age", intBetween(0, 130)))
            .withRule(requiredRule(p.name, "name", length(2, 5), oneOf("Kate", "John")))
            .withRule(requiredRule(p.cat, "cat", catValidationType));

    ValidationTestData.Person p = new ValidationTestData.Person("Joko", 23,
        new ValidationTestData.Cat("skibidi", 100));

    Mono<ValidationTestData.Person> validate = Validator.validate(p, personValidationType);

    StepVerifier.create(validate)
        .expectErrorSatisfies(throwable -> {
          assertTrue(throwable instanceof ValidationException);
          assertNotNull(throwable.getMessage());
          assertEquals(2, throwable.getMessage().split("\\.").length);
        })
        .verify();
  }
}
