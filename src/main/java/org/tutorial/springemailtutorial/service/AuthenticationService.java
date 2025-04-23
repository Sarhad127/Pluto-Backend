package org.tutorial.springemailtutorial.service;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.tutorial.springemailtutorial.customExceptions.DuplicateUserException;
import org.tutorial.springemailtutorial.dto.LoginUserDto;
import org.tutorial.springemailtutorial.dto.RegisterUserDto;
import org.tutorial.springemailtutorial.dto.VerifyUserDto;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public void signup(RegisterUserDto input) {
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new DuplicateUserException("Email is already taken.");
        }

        if (userRepository.existsByUsername(input.getUsername())) {
            throw new DuplicateUserException("Username is already taken.");
        }
        User user = new User(input.getUsername(), input.getEmail(), passwordEncoder.encode(input.getPassword()));
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);
        sendVerificationEmail(user);
        userRepository.save(user);
    }

    public User authenticate(LoginUserDto input) {
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            input.getEmail(),
                            input.getPassword()
                    )
            );
        } catch (DisabledException ex) {
            throw new DisabledException("Account not verified");
        }

        return user;
    }

    public void verifyUser(VerifyUserDto input) {
        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Verification code has expired");
            }
            if (user.getVerificationCode().equals(input.getVerificationCode())) {
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiresAt(null);
                userRepository.save(user);
            } else {
                throw new RuntimeException("Invalid verification code");
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public void resendVerificationCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.isEnabled()) {
                throw new RuntimeException("Account is already verified");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
            sendVerificationEmail(user);
            userRepository.save(user);
        } else {
            throw new RuntimeException("User not found");
        }
    }

    private void sendVerificationEmail(User user) {
        String subject = "Account Verification";
        String verificationCode = user.getVerificationCode();
        String htmlMessage = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>Email Verification</title>" +
                "</head>" +
                "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f9fafb;'>" +
                "<div style='max-width: 600px; margin: 40px auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);'>" +
                "<div style='text-align: center;'>" +
                "<h2 style='color: #111827;'>Welcome to Pluto!</h2>" +
                "<p style='font-size: 16px; color: #4b5563;'>You're almost ready to start managing your tasks like a pro. Enter the verification code below to complete your sign-up:</p>" +
                "<div style='margin: 30px 0; padding: 20px; background-color: #f3f4f6; border-radius: 8px;'>" +
                "<span style='font-size: 24px; font-weight: bold; color: #2563eb; letter-spacing: 2px;'>" + verificationCode + "</span>" +
                "</div>" +
                "<p style='font-size: 14px; color: #6b7280;'>Didn't sign up for Pluto? No worries — you can safely ignore this message.</p>" +
                "<p style='font-size: 14px; color: #9ca3af; margin-top: 30px;'>See you soon!<br>The Pluto Team</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";


        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}