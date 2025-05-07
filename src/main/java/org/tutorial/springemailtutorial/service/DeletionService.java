package org.tutorial.springemailtutorial.service;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.tutorial.springemailtutorial.dto.VerifyUserByUsernameDto;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@AllArgsConstructor
public class DeletionService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    public void initiateDeletion(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setVerificationCode(generateCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        sendDeletionEmail(user);
        userRepository.save(user);
    }

    public void confirmAndDeleteUser(VerifyUserByUsernameDto input) {
        User user = userRepository.findByUsername(input.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Code expired");
        }

        if (!user.getVerificationCode().equals(input.getVerificationCode())) {
            throw new RuntimeException("Invalid code");
        }

        userRepository.delete(user);
    }

    private String generateCode() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }

    private void sendDeletionEmail(User user) {
        String subject = "Confirm Your Pluto Account Deletion";
        String code = user.getVerificationCode();
        String html = "<div style='font-family: Arial, sans-serif;'>" +
                "<h2>Confirm Deletion</h2>" +
                "<p>You requested to delete your Pluto account. If this was you, enter this code:</p>" +
                "<h3 style='color: red;'>" + code + "</h3>" +
                "<p>If you didn't request this, ignore this email.</p>" +
                "</div>";
        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, html);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
