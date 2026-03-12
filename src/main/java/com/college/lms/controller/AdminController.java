package com.college.lms.controller;

import com.college.lms.model.LeaveRequest;
import com.college.lms.repo.LeaveRequestRepository;
import com.college.lms.service.LeaveService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
  private final LeaveRequestRepository leaveRequestRepository;
  private final LeaveService leaveService;

  public AdminController(LeaveRequestRepository leaveRequestRepository, LeaveService leaveService) {
    this.leaveRequestRepository = leaveRequestRepository;
    this.leaveService = leaveService;
  }

  @GetMapping("/stats")
  @PreAuthorize("hasRole('ADMIN')")
  public Map<String, Long> getStats() {
    Map<String, Long> stats = new HashMap<>();
    stats.put("total", leaveRequestRepository.count());
    stats.put("pending", leaveRequestRepository.countByStatus("PENDING"));
    stats.put("approved", leaveRequestRepository.countByStatus("APPROVED"));
    stats.put("rejected", leaveRequestRepository.countByStatus("REJECTED"));
    return stats;
  }

  @GetMapping("/history")
  @PreAuthorize("hasRole('ADMIN')")
  public List<HistoryResponse> history() {
    return leaveService.listAllHistory().stream().map(HistoryResponse::from).toList();
  }

  public static class HistoryResponse {
    public Long id;
    public String studentUsername;
    public String department;
    public String section;
    public String year;
    public String reason;
    public String fromDate;
    public String toDate;
    public String fromTime;
    public String toTime;
    public String status;
    public String reviewedBy;
    public String approvedBy;
    public String rejectedBy;

    public static HistoryResponse from(LeaveRequest request) {
      HistoryResponse response = new HistoryResponse();
      response.id = request.getId();
      response.studentUsername = request.getStudent().getUsername();
      response.department = request.getDepartment();
      response.section = request.getSection();
      response.year = request.getYear();
      response.reason = request.getReason();
      response.fromDate = request.getFromDate() != null ? request.getFromDate().toString() : null;
      response.toDate = request.getToDate() != null ? request.getToDate().toString() : null;
      response.fromTime = request.getFromTime() != null ? request.getFromTime().toString() : null;
      response.toTime = request.getToTime() != null ? request.getToTime().toString() : null;
      response.status = request.getStatus();
      response.reviewedBy = request.getReviewedBy() != null ? request.getReviewedBy().getUsername() : null;
      response.approvedBy = "APPROVED".equalsIgnoreCase(response.status) ? response.reviewedBy : null;
      response.rejectedBy = "REJECTED".equalsIgnoreCase(response.status) ? response.reviewedBy : null;
      return response;
    }
  }
}
