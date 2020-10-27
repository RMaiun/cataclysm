package com.mairo.cataclysm.formatter;

import com.mairo.cataclysm.dto.IdDto;
import org.springframework.stereotype.Service;

@Service
public class StoredIdMessageFormatter implements MessageFormatter<IdDto> {

  @Override
  public String format(IdDto data) {
    return String.format("%s New round was stored with id %s %s",
        PREFIX, data.getId(), SUFFIX);
  }
}
