package com.mairo.cataclysm.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BinaryFileDto {
  private byte[] data;
  private String fileName;
  private String extension;
}
