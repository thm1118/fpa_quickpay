package com.fintech.quickpay.controller;

import com.fintech.quickpay.dto.UserDTO;
import com.fintech.quickpay.security.CurrentUser;
import com.fintech.quickpay.security.UserPrincipal;
import com.fintech.quickpay.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(userService.convertToDTO(principal.getUser()));
    }

    @PostMapping("/payment-password")
    public ResponseEntity<Map<String, String>> setPaymentPassword(
            @CurrentUser UserPrincipal principal,
            @RequestBody Map<String, String> request) {
        String paymentPassword = request.get("paymentPassword");
        userService.setPaymentPassword(principal.getUser(), paymentPassword);
        return ResponseEntity.ok(Map.of("message", "Payment password set successfully"));
    }
}
