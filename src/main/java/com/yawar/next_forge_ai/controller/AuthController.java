package com.yawar.next_forge_ai.controller;


import com.yawar.next_forge_ai.dto.*;
import com.yawar.next_forge_ai.service.AuthService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AuthController {

    AuthService authService;

    @PostMapping(
            value = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody(required = true) RegisterRequest request) throws MessagingException {
        RegisterResponse response=authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/verify-otp/{userId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuthResponse> verifyOtpAndFilnalizeRegister(
            @PathVariable(value = "userId") String userId,
            @Valid @RequestBody(required = true) OtpRequest request){
        AuthResponse response = authService.verifyOtpAndFilnalizeRegister(userId,request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody(required = true) LoginRequest request){
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/forgot-password",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody(required = true) ForgotPasswordRequest request){
        authService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("message","Password Reset Link Send Successfully"));
    }


    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password has been successfully reset. You can now login."));
    }

}
