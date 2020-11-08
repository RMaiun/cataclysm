package com.mairo.cataclysm.formatter;

import com.mairo.cataclysm.dto.SubscriptionResultDto;
import org.springframework.stereotype.Service;

@Service
public class LinkTidFormatter implements MessageFormatter<SubscriptionResultDto> {

  @Override
  public String format(SubscriptionResultDto data) {
    return String.format("%s Notifications were linked for %s%s", PREFIX, data.getSubscribedSurname(), SUFFIX);
  }
}
