package com.mairo.cataclysm.validation;

import static com.mairo.cataclysm.validation.ValidationSchemas.listLastRoundsValidationSchema;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    FindLastRoundsDto dto = new FindLastRoundsDto("S2/2020", 1000);
    Mono<FindLastRoundsDto> validate = Validator.validate(dto, listLastRoundsValidationSchema);
    validate.subscribe(result -> {
      assertEquals(dto, result);
    });
  }

  @Test
  void testInvalidSeason() {
    FindLastRoundsDto dto = new FindLastRoundsDto("S22/4", 3004);
    Mono<FindLastRoundsDto> validate = Validator.validate(dto, listLastRoundsValidationSchema);
    StepVerifier.create(validate)
        .expectError(ValidationException.class)
        .verify();
  }

  @Test
  void testInvalidQty() {
    FindLastRoundsDto dto = new FindLastRoundsDto("S2/2020", 4000);
    Mono<FindLastRoundsDto> validate = Validator.validate(dto, listLastRoundsValidationSchema);
    StepVerifier.create(validate)
        .expectError(ValidationException.class)
        .verify();

  }

  @Test
  public void testInvalidBothValues() {
    FindLastRoundsDto dto = new FindLastRoundsDto("S22/4", 4000);
    Mono<FindLastRoundsDto> validate = Validator.validate(dto, listLastRoundsValidationSchema);
    StepVerifier.create(validate)
        .expectErrorSatisfies(throwable -> {
          assertTrue(throwable instanceof ValidationException);
          assertNotNull(throwable.getMessage());
          assertEquals(2, throwable.getMessage().split("\\.").length);
        })
        .verify();
  }
}
