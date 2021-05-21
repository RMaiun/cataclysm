package com.mairo.cataclysm.core.service;

import com.mairo.cataclysm.core.dto.BinaryFileDto;
import com.mairo.cataclysm.core.dto.GenerateStatsDocumentDto;
import com.mairo.cataclysm.core.validation.ValidationTypes;
import com.mairo.cataclysm.core.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class ReportGeneratorService {

  private final StatisticsService statisticsService;
  private final XlsxWriter xlsxWriter;
  private final ReportStupidCacheService cacheService;

  public Mono<BinaryFileDto> generateXslxReport(GenerateStatsDocumentDto dto) {
    return Validator.validate(dto, ValidationTypes.generateStatsDocumentValidationType)
        .flatMap(this::processReportGeneration);
  }

  private Mono<BinaryFileDto> processReportGeneration(GenerateStatsDocumentDto dto) {
    return cacheService.get(String.format(XlsxWriter.REPORT_NAME_WITH_EXT, dto.getSeason()))
        .flatMap(res -> res.map(Mono::just).orElseGet(() -> generateNewReport(dto)));
  }

  private Mono<BinaryFileDto> generateNewReport(GenerateStatsDocumentDto dto) {
    return statisticsService.seasonStatisticsRows(dto.getSeason())
        .publishOn(Schedulers.elastic())
        .flatMap(stats -> xlsxWriter.generateDocument(stats, dto.getSeason()))
        .flatMap(cacheService::memorize);
  }
}
