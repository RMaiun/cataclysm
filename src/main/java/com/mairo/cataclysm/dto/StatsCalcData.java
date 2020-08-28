package com.mairo.cataclysm.dto;

import lombok.Value;

@Value
public class StatsCalcData {
  long pid;
  String player;
  int points;
  int qty = 1;

}
