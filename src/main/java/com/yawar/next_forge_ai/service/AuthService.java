package com.yawar.next_forge_ai.service;


import com.yawar.next_forge_ai.dto.*;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;

public interface AuthService {
    void register(@Valid RegisterRequest request) throws MessagingException;

    AuthResponse verifyOtpAndFilnalizeRegister(String userId, @Valid OtpRequest request);

    AuthResponse login(@Valid LoginRequest request);

    void forgotPassword(@Valid ForgotPasswordRequest request);

    void resetPassword(@Valid ResetPasswordRequest request);
}
