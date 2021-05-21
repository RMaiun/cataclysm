package com.mairo.cataclysm.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LinkTidDto {

  private String tid;
  private String nameToLink;
  private String moderator;
}
