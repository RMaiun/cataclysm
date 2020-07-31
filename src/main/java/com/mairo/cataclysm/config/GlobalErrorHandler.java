package com.mairo.cataclysm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.dto.ErrorResponse;
import com.mairo.cataclysm.exception.CataRuntimeException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
@RequiredArgsConstructor
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

  private final Logger logger = LogManager.getLogger(GlobalErrorHandler.class);

  private final ObjectMapper objectMapper;

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
    ex.printStackTrace();
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
    return exchange.getResponse().writeWith(Mono.fromSupplier(() -> {
      if (ex instanceof CataRuntimeException) {
        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
      } else {
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
      }
      DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
      String[] reasons = ex.getMessage().split("\\.");
      try {
        return bufferFactory.wrap(objectMapper.writeValueAsBytes(new ErrorResponse(Arrays.asList(reasons))));
      } catch (Exception e) {
        logger.warn("Error writing response", ex);
        return bufferFactory.wrap(new byte[0]);
      }
    }));
  }
}
