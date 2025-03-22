package com.example.datnsd26.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debug")
public class SessionDebugController {
    @GetMapping("/session")
    public ResponseEntity<?> checkSession(HttpSession session) {
        return ResponseEntity.ok("Session ID: " + session.getId());
    }
}
