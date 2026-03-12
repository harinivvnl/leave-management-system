package com.college.lms.service;

import com.college.lms.dto.LeaveRequestDto;
import com.college.lms.dto.LeaveStatusUpdateDto;
import com.college.lms.dto.MedicalRegularizationRequestDto;
import com.college.lms.model.LeaveRequest;
import com.college.lms.model.User;
import com.college.lms.repo.LeaveRequestRepository;
import com.college.lms.repo.UserRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LeaveService {
  private final LeaveRequestRepository leaveRequestRepository;
  private final UserRepository userRepository;
  private final Path attachmentRoot;

  public LeaveService(
      LeaveRequestRepository leaveRequestRepository,
      UserRepository userRepository,
      @Value("${app.upload-dir:uploads}") String uploadDir
  ) {
    this.leaveRequestRepository = leaveRequestRepository;
    this.userRepository = userRepository;
    this.attachmentRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    try {
      Files.createDirectories(this.attachmentRoot);
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to initialize attachment storage directory", exception);
    }
  }

  public LeaveRequest submit(LeaveRequestDto dto) {
    User student = getCurrentUser();
    boolean isEventReason = dto.getReason() != null
        && dto.getReason().trim().toUpperCase().contains("EVENT");
    if (isEventReason && (dto.getFromTime() == null || dto.getToTime() == null)) {
      throw new IllegalArgumentException("From time and to time are required for EVENTS leave requests");
    }

    LeaveRequest request = new LeaveRequest();
    request.setStudent(student);
    request.setReason(dto.getReason());
    String requestSection = StringUtils.hasText(student.getSection())
      ? student.getSection()
      : dto.getSection();
    String requestDepartment = StringUtils.hasText(student.getDepartment())
      ? student.getDepartment()
      : dto.getDepartment();
    String requestYear = StringUtils.hasText(student.getYear())
      ? student.getYear()
      : dto.getYear();
    request.setSection(requestSection);
    request.setDepartment(requestDepartment);
    request.setYear(requestYear);
    request.setFromDate(dto.getFromDate());
    request.setToDate(dto.getToDate());
    request.setFromTime(isEventReason ? dto.getFromTime() : null);
    request.setToTime(isEventReason ? dto.getToTime() : null);
    request.setStatus("PENDING");
    request.setMedicalRegularization(Boolean.FALSE);
    return leaveRequestRepository.save(request);
  }

  public LeaveRequest submitMedicalRegularization(MedicalRegularizationRequestDto dto, MultipartFile attachment) {
    User student = getCurrentUser();
    if (attachment == null || attachment.isEmpty()) {
      throw new IllegalArgumentException("Attachment is required for medical regularization requests");
    }

    String reason = dto.getReason() == null ? "" : dto.getReason().trim().toUpperCase();
    boolean medicalReason = reason.contains("MEDICAL")
        || reason.contains("NOT WELL")
        || reason.contains("UNWELL")
        || reason.contains("SICK")
      || reason.contains("FEVER")
      || reason.contains("ILL")
      || reason.contains("HEALTH")
      || reason.contains("HOSPITAL")
      || reason.contains("CLINIC")
      || reason.contains("DOCTOR");
    if (!medicalReason) {
      throw new IllegalArgumentException("Medical regularization is only allowed for medical or not-well reasons");
    }

    LeaveRequest request = new LeaveRequest();
    request.setStudent(student);
    request.setReason(dto.getReason());
    String requestSection = StringUtils.hasText(student.getSection())
        ? student.getSection()
        : null;
    String requestDepartment = StringUtils.hasText(student.getDepartment())
        ? student.getDepartment()
        : null;
    String requestYear = StringUtils.hasText(student.getYear())
        ? student.getYear()
        : null;
    request.setSection(requestSection);
    request.setDepartment(requestDepartment);
    request.setYear(requestYear);
    request.setFromDate(dto.getFromDate());
    request.setToDate(dto.getToDate());
    request.setFromTime(null);
    request.setToTime(null);
    request.setStatus("PENDING");
    request.setMedicalRegularization(Boolean.TRUE);
    String originalFileName = attachment.getOriginalFilename();
    request.setAttachmentFileName(originalFileName);
    request.setAttachmentContentType(attachment.getContentType());
    try {
      String extension = StringUtils.getFilenameExtension(originalFileName);
      String storedFileName = UUID.randomUUID() + (StringUtils.hasText(extension) ? "." + extension : "");
      Path targetPath = attachmentRoot.resolve(storedFileName).normalize();
      try (InputStream inputStream = attachment.getInputStream()) {
        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
      }
      request.setAttachmentStoragePath(targetPath.toString());
      request.setAttachmentData(null);
    } catch (IOException exception) {
      throw new IllegalArgumentException("Unable to save attachment", exception);
    }
    return leaveRequestRepository.save(request);
  }

  public List<LeaveRequest> listMine() {
    User student = getCurrentUser();
    return leaveRequestRepository.findByStudent(student);
  }

  public List<LeaveRequest> listPending() {
    User currentUser = getCurrentUser();
    if (currentUser.getRole().name().equals("ADMIN")) {
      return leaveRequestRepository.findByStatus("PENDING");
    }
    if (!StringUtils.hasText(currentUser.getDepartment())
        || !StringUtils.hasText(currentUser.getSection())
        || !StringUtils.hasText(currentUser.getYear())) {
      throw new AccessDeniedException("Faculty profile must include department, section and year");
    }
    return leaveRequestRepository.findByStatusAndDepartmentIgnoreCaseAndSectionIgnoreCaseAndYearIgnoreCase(
        "PENDING",
        currentUser.getDepartment(),
        currentUser.getSection(),
        currentUser.getYear()
    );
  }

  public LeaveRequest updateStatus(Long id, LeaveStatusUpdateDto dto) {
    User currentUser = getCurrentUser();
    LeaveRequest request = leaveRequestRepository.findById(id).orElseThrow();
    boolean isAdmin = currentUser.getRole().name().equals("ADMIN");
    boolean sameDepartment = StringUtils.hasText(currentUser.getDepartment())
        && currentUser.getDepartment().equalsIgnoreCase(request.getDepartment());
    boolean sameSection = StringUtils.hasText(currentUser.getSection())
        && currentUser.getSection().equalsIgnoreCase(request.getSection());
    boolean sameYear = StringUtils.hasText(currentUser.getYear())
        && currentUser.getYear().equalsIgnoreCase(request.getYear());

    if (!isAdmin && !(sameDepartment && sameSection && sameYear)) {
      throw new AccessDeniedException("You can only review leave requests from your own department, section and year");
    }

    request.setStatus(dto.getStatus());
    request.setReviewedBy(currentUser);
    return leaveRequestRepository.save(request);
  }

  public List<LeaveRequest> listAllHistory() {
    return leaveRequestRepository.findAllByOrderByDepartmentAscSectionAscYearAscIdDesc();
  }

  public LeaveRequest getAttachmentForCurrentUser(Long id) {
    User currentUser = getCurrentUser();
    LeaveRequest request = leaveRequestRepository.findById(id).orElseThrow();
    boolean isAdmin = currentUser.getRole().name().equals("ADMIN");
    boolean isFaculty = currentUser.getRole().name().equals("FACULTY");
    boolean isStudentOwner = currentUser.getRole().name().equals("STUDENT")
        && request.getStudent() != null
        && currentUser.getId().equals(request.getStudent().getId());

    boolean facultyCanView = false;
    if (isFaculty) {
      boolean sameDepartment = StringUtils.hasText(currentUser.getDepartment())
          && currentUser.getDepartment().equalsIgnoreCase(request.getDepartment());
      boolean sameSection = StringUtils.hasText(currentUser.getSection())
          && currentUser.getSection().equalsIgnoreCase(request.getSection());
      boolean sameYear = StringUtils.hasText(currentUser.getYear())
          && currentUser.getYear().equalsIgnoreCase(request.getYear());
      facultyCanView = sameDepartment && sameSection && sameYear;
    }

    if (!isAdmin && !facultyCanView && !isStudentOwner) {
      throw new AccessDeniedException("You are not allowed to view this attachment");
    }
    boolean hasDbAttachment = request.getAttachmentData() != null && request.getAttachmentData().length > 0;
    boolean hasFileAttachment = StringUtils.hasText(request.getAttachmentStoragePath());
    if (!hasDbAttachment && !hasFileAttachment) {
      throw new IllegalArgumentException("No attachment available for this leave request");
    }
    return request;
  }

  public byte[] loadAttachmentBytes(LeaveRequest request) {
    if (request.getAttachmentData() != null && request.getAttachmentData().length > 0) {
      return request.getAttachmentData();
    }
    if (StringUtils.hasText(request.getAttachmentStoragePath())) {
      try {
        return Files.readAllBytes(Paths.get(request.getAttachmentStoragePath()));
      } catch (IOException exception) {
        throw new IllegalArgumentException("Unable to read attachment from storage", exception);
      }
    }
    throw new IllegalArgumentException("No attachment available for this leave request");
  }

  private User getCurrentUser() {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByUsername(username).orElseThrow();
  }
}
