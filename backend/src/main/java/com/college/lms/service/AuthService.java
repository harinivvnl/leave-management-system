package com.college.lms.service;

import com.college.lms.dto.AuthRequest;
import com.college.lms.dto.AuthResponse;
import com.college.lms.model.Role;
import com.college.lms.model.User;
import com.college.lms.repo.UserRepository;
import com.college.lms.security.JwtService;
import java.util.Optional;
import org.springframework.util.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      AuthenticationManager authenticationManager,
      JwtService jwtService
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
  }

  public AuthResponse register(AuthRequest request, Role role) {
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new IllegalArgumentException("Username already exists");
    }

    if (role == Role.STUDENT || role == Role.FACULTY) {
      if (!StringUtils.hasText(request.getDepartment())) {
        throw new IllegalArgumentException("Department is required for Student and Faculty");
      }
      if (!StringUtils.hasText(request.getSection())) {
        throw new IllegalArgumentException("Section is required for Student and Faculty");
      }
      if (!StringUtils.hasText(request.getYear())) {
        throw new IllegalArgumentException("Year is required for Student and Faculty");
      }
    }

    User user = new User();
    user.setUsername(request.getUsername().trim());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRole(role);
    user.setDepartment(StringUtils.hasText(request.getDepartment()) ? request.getDepartment().trim() : null);
    user.setSection(StringUtils.hasText(request.getSection()) ? request.getSection().trim() : null);
    user.setYear(StringUtils.hasText(request.getYear()) ? request.getYear().trim() : null);
    userRepository.save(user);
    String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
    return new AuthResponse(token, user.getRole().name());
  }

  public AuthResponse login(AuthRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
    );
    Optional<User> user = userRepository.findByUsername(request.getUsername());
    User currentUser = user.orElseThrow();
    String token = jwtService.generateToken(currentUser.getUsername(), currentUser.getRole().name());
    return new AuthResponse(token, currentUser.getRole().name());
  }
}
