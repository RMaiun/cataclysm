package com.mairo.cataclysm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.domain.Round;
import com.mairo.cataclysm.domain.Season;
import com.mairo.cataclysm.dto.BinaryFileDto;
import com.mairo.cataclysm.repository.PlayerRepository;
import com.mairo.cataclysm.repository.RoundRepository;
import com.mairo.cataclysm.repository.SeasonRepository;
import com.mairo.cataclysm.utils.MonoSupport;
import io.vavr.control.Try;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

@Service
@RequiredArgsConstructor
public class ExportService {

  private final SeasonRepository seasonRepository;
  private final PlayerRepository playerRepository;
  private final RoundRepository roundRepository;
  private final ObjectMapper objectMapper;

  private final String SEASONS = "seasons.json";
  private final String PLAYERS = "players.json";
  private final String ROUNDS = "rounds_%s.json";

  public Mono<BinaryFileDto> export(LocalDateTime before) {
    return Mono.zip(findAllSeasons(), findAllPlayers(), findRoundsBeforeDate(before))
        .flatMap(this::prepareZipArchive);
  }

  private Mono<BinaryFileDto> prepareZipArchive(Tuple3<List<Season>, List<Player>, List<Round>> data) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ZipOutputStream zipOutputStream = new ZipOutputStream(bos);
    Try<ZipOutputStream> zosResult = Try.of(() -> zipOutputStream)
        .flatMap(zos -> writeZipEntry(data.getT1(), SEASONS, zos))
        .flatMap(zos -> writeZipEntry(data.getT2(), PLAYERS, zos))
        .flatMap(zos -> prepareRoundsBySeason(data.getT3(), data.getT1(), zos))
        .andFinallyTry(zipOutputStream::close);

    return MonoSupport.fromTry(zosResult)
        .map(__ -> bos.toByteArray())
        .map(this::prepareResultDto);
  }

  private BinaryFileDto prepareResultDto(byte[] bytes) {
    LocalDateTime now = LocalDateTime.now();
    int day = now.getDayOfMonth();
    String month = now.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
    int year = now.getYear();
    String date = String.format("%d_%s_%d", day, month, year);
    String ARCHIVE_NAME = "cata_dump_%s";
    String archiveName = String.format(ARCHIVE_NAME, date);
    return new BinaryFileDto(bytes, archiveName, "zip");
  }

  private Try<ZipOutputStream> prepareRoundsBySeason(List<Round> rounds, List<Season> seasons, ZipOutputStream zos) {
    Map<Long, String> seasonsData = seasons.stream().collect(Collectors.toMap(Season::getId, Season::getName));
    Stream<Entry<String, List<Round>>> roundsPerSeasonStream = rounds.stream()
        .collect(Collectors.groupingBy(r -> seasonsData.get(r.getSeasonId())))
        .entrySet()
        .stream();
    io.vavr.collection.List<Entry<String, List<Round>>> vavrRoundsList = io.vavr.collection.Stream.ofAll(roundsPerSeasonStream).toList();
    return writeRoundsRecursively(vavrRoundsList, Try.of(() -> zos));
  }

  private Try<ZipOutputStream> writeRoundsRecursively(io.vavr.collection.List<Entry<String, List<Round>>> roundsPerSeason, Try<ZipOutputStream> zos) {
    if (roundsPerSeason.isEmpty()) {
      return zos;
    }
    Try<ZipOutputStream> modifiedZos = zos.flatMap(zosVal -> {
      Entry<String, List<Round>> head = roundsPerSeason.head();
      String fileName = head.getKey().replace("|", "_");
      return writeZipEntry(head.getValue(), String.format(ROUNDS, fileName), zosVal);
    });
    return writeRoundsRecursively(roundsPerSeason.tail(), modifiedZos);
  }

  private Try<ZipOutputStream> writeZipEntry(Object data, String name, ZipOutputStream zos) {
    return Try.of(() -> {
      ZipEntry e = new ZipEntry(name);
      zos.putNextEntry(e);
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(new CloseShieldOutputStream(zos), data);
      zos.closeEntry();
      return zos;
    });
  }

  private Mono<List<Season>> findAllSeasons() {
    return seasonRepository.listAll();
  }

  private Mono<List<Player>> findAllPlayers() {
    return playerRepository.listAll();
  }

  private Mono<List<Round>> findRoundsBeforeDate(LocalDateTime before) {
    return roundRepository.listLastRoundsBeforeDate(before);
  }
}
