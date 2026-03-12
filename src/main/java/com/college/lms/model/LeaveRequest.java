package com.college.lms.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "leave_requests")
public class LeaveRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "student_id", nullable = false)
  private User student;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewed_by")
  private User reviewedBy;

  private String reason;

  private String section;

  private String department;

  private String year;

  private LocalDate fromDate;

  private LocalDate toDate;

  private LocalTime fromTime;

  private LocalTime toTime;

  private String status;

  private Boolean medicalRegularization;

  private String attachmentFileName;

  private String attachmentContentType;

  private String attachmentStoragePath;

  @Lob
  @jakarta.persistence.Column(columnDefinition = "LONGBLOB")
  private byte[] attachmentData;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getStudent() {
    return student;
  }

  public void setStudent(User student) {
    this.student = student;
  }

  public User getReviewedBy() {
    return reviewedBy;
  }

  public void setReviewedBy(User reviewedBy) {
    this.reviewedBy = reviewedBy;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getSection() {
    return section;
  }

  public void setSection(String section) {
    this.section = section;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public LocalDate getFromDate() {
    return fromDate;
  }

  public void setFromDate(LocalDate fromDate) {
    this.fromDate = fromDate;
  }

  public LocalDate getToDate() {
    return toDate;
  }

  public void setToDate(LocalDate toDate) {
    this.toDate = toDate;
  }

  public LocalTime getFromTime() {
    return fromTime;
  }

  public void setFromTime(LocalTime fromTime) {
    this.fromTime = fromTime;
  }

  public LocalTime getToTime() {
    return toTime;
  }

  public void setToTime(LocalTime toTime) {
    this.toTime = toTime;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Boolean getMedicalRegularization() {
    return medicalRegularization;
  }

  public void setMedicalRegularization(Boolean medicalRegularization) {
    this.medicalRegularization = medicalRegularization;
  }

  public String getAttachmentFileName() {
    return attachmentFileName;
  }

  public void setAttachmentFileName(String attachmentFileName) {
    this.attachmentFileName = attachmentFileName;
  }

  public String getAttachmentContentType() {
    return attachmentContentType;
  }

  public void setAttachmentContentType(String attachmentContentType) {
    this.attachmentContentType = attachmentContentType;
  }

  public String getAttachmentStoragePath() {
    return attachmentStoragePath;
  }

  public void setAttachmentStoragePath(String attachmentStoragePath) {
    this.attachmentStoragePath = attachmentStoragePath;
  }

  public byte[] getAttachmentData() {
    return attachmentData;
  }

  public void setAttachmentData(byte[] attachmentData) {
    this.attachmentData = attachmentData;
  }
}
