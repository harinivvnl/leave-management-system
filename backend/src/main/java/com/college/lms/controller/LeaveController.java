package com.college.lms.controller;

import com.college.lms.dto.LeaveRequestDto;
import com.college.lms.dto.LeaveStatusUpdateDto;
import com.college.lms.dto.MedicalRegularizationRequestDto;
import com.college.lms.model.LeaveRequest;
import com.college.lms.service.LeaveService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {
  private final LeaveService leaveService;

  public LeaveController(LeaveService leaveService) {
    this.leaveService = leaveService;
  }

  @PostMapping
  @PreAuthorize("hasRole('STUDENT')")
  public LeaveRequest submit(@Valid @RequestBody LeaveRequestDto dto) {
    return leaveService.submit(dto);
  }

  @PostMapping(value = "/medical-regularization", consumes = "multipart/form-data")
  @PreAuthorize("hasRole('STUDENT')")
  public LeaveRequest submitMedicalRegularization(
      @RequestParam("reason") String reason,
      @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
      @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
      @RequestParam("attachment") MultipartFile attachment
  ) {
    MedicalRegularizationRequestDto dto = new MedicalRegularizationRequestDto();
    dto.setReason(reason);
    dto.setFromDate(fromDate);
    dto.setToDate(toDate);
    return leaveService.submitMedicalRegularization(dto, attachment);
  }

  @GetMapping("/mine")
  @PreAuthorize("hasRole('STUDENT')")
  public List<LeaveResponse> listMine() {
    return leaveService.listMine().stream()
        .map(LeaveResponse::from)
        .collect(Collectors.toList());
  }

  @GetMapping("/pending")
  @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
  public List<LeaveResponse> listPending() {
    return leaveService.listPending().stream()
        .map(LeaveResponse::from)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}/attachment")
  @PreAuthorize("hasRole('STUDENT') or hasRole('FACULTY') or hasRole('ADMIN')")
  public ResponseEntity<byte[]> viewAttachment(@PathVariable Long id) {
    LeaveRequest request = leaveService.getAttachmentForCurrentUser(id);
    byte[] fileData = leaveService.loadAttachmentBytes(request);
    String contentType = request.getAttachmentContentType();
    MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
    if (contentType != null && !contentType.isBlank()) {
      mediaType = MediaType.parseMediaType(contentType);
    }
    String fileName = request.getAttachmentFileName() != null && !request.getAttachmentFileName().isBlank()
        ? request.getAttachmentFileName()
        : "attachment";

    return ResponseEntity.ok()
        .contentType(mediaType)
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
      .body(fileData);
  }

  @PutMapping("/{id}/status")
  @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
  public LeaveResponse updateStatus(
      @PathVariable Long id,
      @Valid @RequestBody LeaveStatusUpdateDto dto
  ) {
    return LeaveResponse.from(leaveService.updateStatus(id, dto));
  }

  public static class LeaveResponse {
    public Long id;
    public String reason;
    public String section;
    public String department;
    public String year;
    public String fromDate;
    public String toDate;
    public String fromTime;
    public String toTime;
    public String status;
    public String studentUsername;
    public Boolean medicalRegularization;
    public String attachmentFileName;
    public String attachmentContentType;

    public static LeaveResponse from(LeaveRequest request) {
      LeaveResponse response = new LeaveResponse();
      response.id = request.getId();
      response.reason = request.getReason();
      response.section = request.getSection();
      response.department = request.getDepartment();
      response.year = request.getYear();
      response.fromDate = request.getFromDate().toString();
      response.toDate = request.getToDate().toString();
      response.fromTime = request.getFromTime() != null ? request.getFromTime().toString() : null;
      response.toTime = request.getToTime() != null ? request.getToTime().toString() : null;
      response.status = request.getStatus();
      response.studentUsername = request.getStudent().getUsername();
      response.medicalRegularization = Boolean.TRUE.equals(request.getMedicalRegularization());
      response.attachmentFileName = request.getAttachmentFileName();
      response.attachmentContentType = request.getAttachmentContentType();
      return response;
    }
  }
}
