package com.mairo.cataclysm.service;

import static org.apache.commons.lang3.StringUtils.capitalize;

import com.mairo.cataclysm.dto.BinaryFileDto;
import com.mairo.cataclysm.dto.SeasonStatsRows;
import com.mairo.cataclysm.exception.WriteXlsxDocumentException;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class XlsxWriter {

  private static final String DEFAULT_FONT = "Arial";
  private static final String REPORT_NAME = "%s_stats";
  private static final String REPORT_EXT = "xlsx";
  static final String REPORT_NAME_WITH_EXT = String.format("%s.%s", REPORT_NAME, REPORT_EXT);

  public Mono<BinaryFileDto> generateDocument(SeasonStatsRows statsRows, String season) {
    XSSFWorkbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet(season);
    prepareHeaders(workbook, sheet, statsRows);
    prepareRounds(workbook, sheet, statsRows);
    prepareTotals(workbook, sheet, statsRows);
    prepareInitialPoints(workbook, sheet, statsRows.getHeaders().size());
    autosizeColumns(statsRows.getHeaders().size(), sheet);
    freezeHeadersWithTotals(sheet);
    return Mono.fromCallable(() -> createDocument(season, workbook, new ByteArrayOutputStream()))
        .subscribeOn(Schedulers.elastic())
        .flatMap(x -> x.fold(err -> Mono.error(new WriteXlsxDocumentException(err)), Mono::just));
  }

  private void freezeHeadersWithTotals(Sheet sheet) {
    sheet.createFreezePane(0, 2);
  }

  private Try<BinaryFileDto> createDocument(String seasonName, XSSFWorkbook workbook, ByteArrayOutputStream bos) {
    return Try.of(() -> {
      workbook.write(bos);
      return new BinaryFileDto(bos.toByteArray(), String.format(REPORT_NAME, seasonName), REPORT_EXT);
    }).andFinallyTry(() -> {
      workbook.close();
      bos.close();
    });
  }

  private void autosizeColumns(int size, Sheet sheet) {
    IntStream.rangeClosed(0, size).forEach(sheet::autoSizeColumn);
  }

  private void prepareHeaders(XSSFWorkbook workbook, Sheet sheet, SeasonStatsRows statsRows) {
    Row row = sheet.createRow(0);
    IntStream.range(0, statsRows.getHeaders().size())
        .forEach(i -> {
          Cell cell = row.createCell(i + 1);
          cell.setCellStyle(defaultCellStyle(workbook));
          cell.setCellValue(capitalize(statsRows.getHeaders().get(i)));
        });
  }

  private void prepareInitialPoints(XSSFWorkbook workbook, Sheet sheet, int size) {
    Row row = sheet.createRow(2);
    IntStream.range(0, size).forEach(i -> {
      Cell cell = row.createCell(i + 1);
      cell.setCellStyle(customColorCellStyle(new XSSFColor(new byte[]{(byte) 226, (byte) 239, (byte) 218}, null), workbook, true));
      cell.setCellValue(1000);
    });
  }

  private void prepareTotals(XSSFWorkbook workbook, Sheet sheet, SeasonStatsRows statsRows) {
    Row row = sheet.createRow(1);
    IntStream.range(0, statsRows.getTotals().size()).forEach(i -> {
      Cell cell = row.createCell(i + 1);
      cell.setCellValue(statsRows.getTotals().get(i));
      cell.setCellStyle(greyCellStyle(workbook));
    });
  }

  private void prepareRounds(XSSFWorkbook workbook, Sheet sheet, SeasonStatsRows statsRows) {
    IntStream.range(0, statsRows.getCreatedDates().size()).forEach(i -> {
      int j = i + 3;
      Row row = sheet.createRow(j);
      Cell cell = row.createCell(0);
      cell.setCellValue(statsRows.getCreatedDates().get(i));
      cell.setCellStyle(greyCellStyle(workbook));
      List<String> games = statsRows.getGames().get(i);
      IntStream.range(0, games.size())
          .forEach(c -> processRoundCell(row, c + 1, games.get(c), workbook));
    });
  }

  private void processRoundCell(Row row, int index, String value, XSSFWorkbook workbook) {
    Cell cell = row.createCell(index);
    switch (value) {
      case "50":
        cell.setCellStyle(customColorCellStyle(color(106, 168, 79), workbook, false));
        cell.setCellValue(50);
        break;
      case "-50":
        cell.setCellStyle(customColorCellStyle(color(224, 102, 102), workbook, false));
        cell.setCellValue(-50);
        break;
      case "25":
        cell.setCellStyle(customColorCellStyle(color(182, 215, 168), workbook, false));
        cell.setCellValue(25);
        break;
      case "-25":
        cell.setCellStyle(customColorCellStyle(color(244, 204, 204), workbook, false));
        cell.setCellValue(-25);
        break;
      default:
        cell.setCellStyle(defaultCellStyle(workbook));
        cell.setCellValue(value);
        break;
    }
  }

  private XSSFColor color(int x1, int x2, int x3) {
    return new XSSFColor(new byte[]{(byte) x1, (byte) x2, (byte) x3}, null);
  }

  private Font defaultFont(XSSFWorkbook workbook) {
    Font font = workbook.createFont();
    font.setFontName(DEFAULT_FONT);
    font.setBold(false);
    font.setFontHeightInPoints((short) 10);
    font.setColor(IndexedColors.BLACK.getIndex());
    return font;
  }

  private CellStyle defaultCellStyle(XSSFWorkbook workbook) {
    return customColorCellStyle(null, workbook, false);
  }

  private CellStyle greyCellStyle(XSSFWorkbook workbook) {
    XSSFColor greyColor = color(220, 220, 220);
    return customColorCellStyle(greyColor, workbook, true);
  }

  private CellStyle customColorCellStyle(XSSFColor color, XSSFWorkbook workbook, boolean border) {
    Font font = defaultFont(workbook);
    XSSFCellStyle style = workbook.createCellStyle();
    style.setFont(font);
    style.setWrapText(true);
    if (color != null) {
      style.setFillForegroundColor(color);
      style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }
    if (border) {
      style.setBorderBottom(BorderStyle.THIN);
      style.setBorderLeft(BorderStyle.THIN);
      style.setBorderRight(BorderStyle.THIN);
      style.setBorderTop(BorderStyle.THIN);
    }
    return style;
  }
}
