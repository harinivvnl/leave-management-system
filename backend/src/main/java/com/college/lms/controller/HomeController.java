package com.college.lms.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

  @GetMapping("/")
  public Map<String, String> home() {
    return Map.of(
        "message", "Leave Management API is running",
        "login", "POST /api/auth/login",
        "register", "POST /api/auth/register?role=STUDENT"
    );
  }
}
