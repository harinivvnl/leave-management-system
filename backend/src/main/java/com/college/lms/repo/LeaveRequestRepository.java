package com.college.lms.repo;

import com.college.lms.model.LeaveRequest;
import com.college.lms.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
  List<LeaveRequest> findByStudent(User student);
  List<LeaveRequest> findByStatus(String status);
  List<LeaveRequest> findByStatusAndDepartment(String status, String department);
  List<LeaveRequest> findByStatusAndDepartmentIgnoreCaseAndSectionIgnoreCaseAndYearIgnoreCase(
      String status,
      String department,
      String section,
      String year
  );
  List<LeaveRequest> findAllByOrderByDepartmentAscSectionAscYearAscIdDesc();
  long countByStatus(String status);
}
