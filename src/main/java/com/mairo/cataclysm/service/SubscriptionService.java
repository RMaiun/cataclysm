package com.mairo.cataclysm.service;

import com.mairo.cataclysm.dto.LinkTidDto;
import com.mairo.cataclysm.dto.SubscriptionActionDto;
import com.mairo.cataclysm.dto.SubscriptionResultDto;
import com.mairo.cataclysm.validation.ValidationTypes;
import com.mairo.cataclysm.validation.Validator;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

  private final UserRightsService userRightsService;
  private final PlayerService playerService;


  public Mono<SubscriptionResultDto> linkTidForPlayer(LinkTidDto dto) {
    return Validator.validate(dto, ValidationTypes.linkTidValidationType)
        .then(userRightsService.checkUserIsAdmin(dto.getModerator()))
        .flatMap(admin -> playerService.enableNotifications(dto.getNameToLink(), dto.getTid()))
        .map(res -> new SubscriptionResultDto(dto.getNameToLink(), dto.getTid(), LocalDateTime.now(), true));
  }

  public Mono<SubscriptionResultDto> updateSubscriptionsStatus(SubscriptionActionDto dto) {
    return Validator.validate(dto, ValidationTypes.subscriptionActionValidationType)
        .flatMap(__ -> playerService.findPlayerByTid(dto.getTid()))
        .map(p -> p.withNotificationsEnabled(dto.isEnableSubscriptions()))
        .flatMap(playerService::updatePlayer)
        .map(p -> new SubscriptionResultDto(p.getSurname(), p.getTid(), LocalDateTime.now(), dto.isEnableSubscriptions()));
  }

}
