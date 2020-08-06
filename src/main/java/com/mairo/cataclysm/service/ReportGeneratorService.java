package com.mairo.cataclysm.service;

import com.mairo.cataclysm.dto.BinaryFileDto;
import com.mairo.cataclysm.dto.GenerateStatsDocumentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReportGeneratorService {

  private final StatisticsService statisticsService;
  private final XlsxWriter xlsxWriter;
  private final ReportStupidCacheService cacheService;

  public Mono<BinaryFileDto> generateXslxReport(GenerateStatsDocumentDto dto) {
    return cacheService.get(String.format(XlsxWriter.REPORT_NAME_WITH_EXT, dto.getSeason()))
        .flatMap(res -> res.map(Mono::just)
            .orElseGet(() -> generateNewReport(dto)));
  }

  private Mono<BinaryFileDto> generateNewReport(GenerateStatsDocumentDto dto) {
    return statisticsService.seasonStatisticsRows(dto.getSeason())
        .flatMap(stats -> xlsxWriter.generateDocument(stats, dto.getSeason()))
        .flatMap(cacheService::memorize);
  }
}
