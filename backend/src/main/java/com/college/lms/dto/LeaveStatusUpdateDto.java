package com.college.lms.dto;

import jakarta.validation.constraints.NotBlank;

public class LeaveStatusUpdateDto {
  @NotBlank
  private String status;

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
