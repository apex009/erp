package com.fy.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionTreeDTO {
  private String id; // For Vue tree key
  private String label;
  private List<Long> permIds;
  private List<PermissionTreeDTO> children;

  public void addChild(PermissionTreeDTO child) {
    if (this.children == null) {
      this.children = new ArrayList<>();
    }
    this.children.add(child);
  }
}
