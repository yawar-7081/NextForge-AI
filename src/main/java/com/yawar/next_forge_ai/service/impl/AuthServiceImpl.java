package com.yawar.next_forge_ai.service.impl;

import com.yawar.next_forge_ai.dto.*;
import com.yawar.next_forge_ai.entity.EmailVerificationToken;
import com.yawar.next_forge_ai.entity.PasswordResetToken;
import com.yawar.next_forge_ai.entity.User;
import com.yawar.next_forge_ai.entity.enums.Provider;
import com.yawar.next_forge_ai.error.BadRequestException;
import com.yawar.next_forge_ai.error.ResourceNotFoundException;
import com.yawar.next_forge_ai.repository.EmailVerificationTokenRepository;
import com.yawar.next_forge_ai.repository.PasswordResetTokenRepository;
import com.yawar.next_forge_ai.repository.UserRepository;
import com.yawar.next_forge_ai.security.CustomUserDetail;
import com.yawar.next_forge_ai.security.JwtService;
import com.yawar.next_forge_ai.service.AuthService;
import com.yawar.next_forge_ai.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetTokenRepository passwordResetTokenRepository;


    @Value("${frontend.url}")
    private String FRONTEND_URL;


    @Transactional
    @Override
    public RegisterResponse register(RegisterRequest request) throws MessagingException {

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if(user != null){
            if(user.isActive() || user.isDeleted()){
                throw new BadRequestException("Already Register");
            }
        }else{
            String generatedUsername = generateUniqueUsername(request.getEmail());

            user = User.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .username(generatedUsername)
                    .password(passwordEncoder.encode(request.getPassword()))
                    .provider(Provider.LOCAL)
                    .isActive(false)
                    .isEmailVerified(false)
                    .isDeleted(false)
                    .build();

            user = userRepository.save(user);
        }



        String otp = generateRandomOtp();

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .email(user.getEmail())
                .otp(otp)
                .expiresAt(System.currentTimeMillis() + (1000 * 60 * 5))
                .build();

        tokenRepository.save(verificationToken);


        log.info("==========================================================");
        log.info("User Id - {}",user.getId());
        log.info("==========================================================");
        emailService.sendOtpEmail(user.getEmail(), otp, user.getName());

        return new RegisterResponse(user.getId());
    }

    private String generateUniqueUsername(String email) {
        String baseUsername = email.substring(0, email.indexOf("@")).replaceAll("[^a-zA-Z0-9_]", "");
        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        return username;
    }

    private String generateRandomOtp() {
        int otpNumber = 100000 + new java.util.Random().nextInt(900000);
        return String.valueOf(otpNumber);
    }


    @Override
    public AuthResponse verifyOtpAndFilnalizeRegister(String userId,OtpRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User",userId));

        if(user.isDeleted()){
            throw new BadRequestException("Invalid User Id - "+userId);
        }

        if(user.isActive()){
            throw new BadRequestException("User Already Registered");
        }

        EmailVerificationToken verificationToken = tokenRepository
                .findTopByEmailAndIsUsedFalseOrderByCreatedAtDesc(user.getEmail())
                .orElseThrow(() -> new BadRequestException("No active verification token found for this user."));

        if (!verificationToken.getOtp().equals(request.getOtp())) {
            throw new BadRequestException("Invalid OTP entered!");
        }

        // 4. Check if OTP has expired
        if (System.currentTimeMillis() > verificationToken.getExpiresAt()) {
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        // 5. Mark token as used so it cannot be reused
        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        // 6. Update user active and verification statuses
        user.setActive(true);
        user.setEmailVerified(true);
        user = userRepository.save(user);

        String token = jwtService.generateToken(null,new CustomUserDetail(user));

        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .token(token)
                .name(user.getName())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword())
        );

        if(!authentication.isAuthenticated()){
            throw new BadRequestException("Invalid Username or Password !");
        }

        CustomUserDetail user = (CustomUserDetail) authentication.getPrincipal();

        String token = jwtService.generateToken(null,user);

        return AuthResponse.builder()
                .userId(user.getUser().getId())
                .username(user.getUser().getUsername())
                .email(user.getUser().getEmail())
                .token(token)
                .name(user.getUser().getName())
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User",request.getEmail()));

        if (!user.isActive() || user.isDeleted()) {
            throw new BadRequestException("Account is inactive or deleted.");
        }


        String resetToken = UUID.randomUUID().toString();


        PasswordResetToken tokenEntity = PasswordResetToken.builder()
                .user(user)
                .token(resetToken)
                .expiresAt(System.currentTimeMillis() + (1000 * 60 * 10)) // Valid for 10 minutes
                .build();

        passwordResetTokenRepository.save(tokenEntity);


        String resetLink = UriComponentsBuilder.fromUriString(FRONTEND_URL)
                .path("/reset-password")
                .queryParam("token", resetToken)
                .toUriString();


        emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), resetLink);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid password reset token."));

        if (resetToken.isUsed()) {
            throw new BadRequestException("This password reset token has already been used.");
        }

        if (System.currentTimeMillis() > resetToken.getExpiresAt()) {
            throw new BadRequestException("Password reset token has expired.");
        }

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
