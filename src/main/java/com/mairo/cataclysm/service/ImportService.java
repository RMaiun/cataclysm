package com.mairo.cataclysm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mairo.cataclysm.domain.Player;
import com.mairo.cataclysm.domain.Round;
import com.mairo.cataclysm.domain.Season;
import com.mairo.cataclysm.dto.ImportDumpData;
import com.mairo.cataclysm.dto.ImportDumpDto;
import com.mairo.cataclysm.exception.DumpException;
import com.mairo.cataclysm.repository.PlayerRepository;
import com.mairo.cataclysm.repository.RoundRepository;
import com.mairo.cataclysm.repository.SeasonRepository;
import com.mairo.cataclysm.utils.MonoSupport;
import io.vavr.control.Try;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ImportService {

  public static final Logger logger = LogManager.getLogger(ImportService.class);

  private final ObjectMapper objectMapper;
  private final SeasonRepository seasonRepository;
  private final PlayerRepository playerRepository;
  private final RoundRepository roundRepository;
  private final UserRightsService userRightsService;


  public Mono<ImportDumpDto> importDump(FilePart file, String moderator) {
    return userRightsService.checkUserIsAdmin(moderator)
        .flatMap(__ -> filePartToInputStream(file))
        .flatMap(this::fetchDumpData)
        .flatMap(MonoSupport::fromTry)
        .flatMap(this::storeDumpData);
  }


  private Mono<ImportDumpDto> storeDumpData(ImportDumpData data) {
    return clearTables()
        .then(importSeasons(data.getSeasonList()))
        .flatMap(sSize -> importPlayers(data.getPlayersList())
            .map(pSize -> new ImportDumpDto().withPlayers(pSize).withSeasons(sSize)))
        .flatMap(result -> importRounds(data.getRoundsList(), data.getSeasonList()).map(result::withRounds));
  }

  private Mono<Integer> clearTables() {
    return Mono.zip(
        roundRepository.removeAll(),
        seasonRepository.removeAll(),
        playerRepository.removeAll())
        .map(t -> t.getT1() + t.getT2() + t.getT3());

  }

  private Mono<InputStream> filePartToInputStream(FilePart file) {
    return file.content()
        .reduce(InputStream.nullInputStream(), (is, d) -> new SequenceInputStream(is, d.asInputStream()));
  }

  private Mono<Try<ImportDumpData>> fetchDumpData(InputStream is) {
    return Mono.fromCallable(() -> {
      logger.info("Start data import from archive dump");
      List<Season> seasonList = new ArrayList<>();
      List<Player> playersList = new ArrayList<>();
      List<Round> roundsList = new ArrayList<>();

      try (ZipInputStream zis = new ZipInputStream(is)) {
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
          if (entry.getName().contains("season")) {
            TypeReference<List<Season>> seasonsTR = new TypeReference<>() {
            };
            processZipEntry(zis, bytes -> seasonList.addAll(readData(bytes, seasonsTR)));
          } else if (entry.getName().contains("players")) {
            TypeReference<List<Player>> playersTR = new TypeReference<>() {
            };
            processZipEntry(zis, bytes -> playersList.addAll(readData(bytes, playersTR)));
          } else {
            TypeReference<List<Round>> roundsTR = new TypeReference<>() {
            };
            processZipEntry(zis, bytes -> roundsList.addAll(readData(bytes, roundsTR)));
          }
        }
      } catch (Throwable e) {
        e.printStackTrace();
        return Try.failure(e);
      }
      logger.info("Data import from archive dump successfully finished");
      return Try.success(new ImportDumpData(seasonList, playersList, roundsList));
    });
  }

  private <T> List<T> readData(byte[] bytes, TypeReference<List<T>> tr) {
    try {
      return objectMapper.readValue(bytes, tr);
    } catch (IOException e) {
      throw new DumpException(e);
    }
  }

  private void processZipEntry(ZipInputStream zis, Consumer<byte[]> consumer) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    IOUtils.copy(zis, bos);
    consumer.accept(bos.toByteArray());
    bos.close();
    zis.closeEntry();
  }

  private Mono<Map<String, Long>> importRounds(List<Round> rounds, List<Season> seasons) {
    Flux<Long> storeRounds = Flux.fromIterable(rounds)
        .flatMap(roundRepository::saveRound);

    Map<Long, String> seasonsMap = seasons.stream()
        .collect(Collectors.toMap(Season::getId, Season::getName));
    Map<String, Long> roundsPerSeasons = rounds.stream()
        .collect(Collectors.groupingBy(r -> seasonsMap.get(r.getSeasonId()), Collectors.counting()));
    return storeRounds.then(Mono.just(roundsPerSeasons));
  }

  private Mono<Integer> importSeasons(List<Season> seasons) {
    return Flux.fromIterable(seasons)
        .flatMap(seasonRepository::saveSeason)
        .then(Mono.just(seasons.size()));
  }

  private Mono<Integer> importPlayers(List<Player> seasons) {
    return Flux.fromIterable(seasons)
        .flatMap(playerRepository::savePlayer)
        .then(Mono.just(seasons.size()));
  }

}
