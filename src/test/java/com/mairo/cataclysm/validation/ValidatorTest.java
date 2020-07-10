package com.mairo.cataclysm.validation;

import com.mairo.cataclysm.data.ValidationTestData;
import com.mairo.cataclysm.dto.FindLastRoundsDto;
import com.mairo.cataclysm.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.mairo.cataclysm.validation.NumberValidationFunctions.intBetween;
import static com.mairo.cataclysm.validation.StringValidationFunctions.length;
import static com.mairo.cataclysm.validation.StringValidationFunctions.oneOf;
import static com.mairo.cataclysm.validation.ValidationRule.requiredRule;
import static com.mairo.cataclysm.validation.ValidationRule.rule;
import static com.mairo.cataclysm.validation.ValidationSchema.schema;
import static com.mairo.cataclysm.validation.ValidationTypes.listLastRoundsValidationType;
import static org.junit.jupiter.api.Assertions.*;

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
  public void testInvalidBothValues() {
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
  public void complexValidationTest() {
    ValidationType<ValidationTestData.Cat> catValidationType = c ->
        schema()
            .witRule(rule(c.sound, "sound", oneOf("mew", "pur")))
            .witRule(rule(c.hungryPrecentage, "hungryPrecentage", intBetween(0, 100)));

    ValidationType<ValidationTestData.Person> personValidationType = p ->
        schema()
            .witRule(requiredRule(p.age, "age", intBetween(0, 130)))
            .witRule(requiredRule(p.name, "name", length(2, 5), oneOf("Kate", "John")))
            .witRule(requiredRule(p.cat, "cat", catValidationType));

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
