package com.mairo.cataclysm.core.service;

import static com.mairo.cataclysm.core.service.XlsxWriter.REPORT_NAME_WITH_EXT;
import static java.util.Optional.ofNullable;

import com.mairo.cataclysm.core.dto.BinaryFileDto;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReportStupidCacheService {

  private final Map<String, BinaryFileDto> cache = new ConcurrentHashMap<>();

  public Mono<BinaryFileDto> memorize(BinaryFileDto dto) {
    String key = dto.getFileName();
    cache.put(key, dto);
    return Mono.just(dto);
  }

  public Mono<Optional<BinaryFileDto>> get(String key) {
    return Mono.just(ofNullable(cache.get(key)));
  }

  public Mono<Optional<BinaryFileDto>> remove(String season) {
    String key = String.format(REPORT_NAME_WITH_EXT, season);
    return Mono.just(ofNullable(cache.remove(key)));
  }

  public Mono<Void> clear() {
    cache.clear();
    return Mono.empty();
  }
}
