package com.mairo.cataclysm.controller;

import com.mairo.cataclysm.core.dto.BinaryFileDto;
import java.io.ByteArrayInputStream;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface BinaryResponseSupport {

  default Mono<ResponseEntity<InputStreamResource>> binaryResponse(Mono<BinaryFileDto> binaryFileMono) {
    return binaryFileMono.map(res -> {
      String fileName = String.format("%s.%s", res.getFileName().replace("|", "_"), res.getExtension());
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment;filename=%s", fileName))
          .body(new InputStreamResource(new ByteArrayInputStream(res.getData())));
    });
  }
}
