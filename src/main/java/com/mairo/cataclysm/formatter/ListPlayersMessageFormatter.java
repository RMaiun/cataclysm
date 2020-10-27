package com.mairo.cataclysm.formatter;

import com.mairo.cataclysm.dto.FoundAllPlayers;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class ListPlayersMessageFormatter implements MessageFormatter<FoundAllPlayers> {

  @Override
  public String format(FoundAllPlayers data) {
    String players = data.getPlayers().stream()
        .map(p -> String.format("%s|%s", p.getId(), StringUtils.capitalize(p.getSurname())))
        .collect(Collectors.joining("\n"));
    return String.format("%s%s%s", PREFIX, players, SUFFIX);
  }
}
