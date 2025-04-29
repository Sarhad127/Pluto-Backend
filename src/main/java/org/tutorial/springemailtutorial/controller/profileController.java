package org.tutorial.springemailtutorial.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tutorial.springemailtutorial.dto.PasswordChangeRequest;
import org.tutorial.springemailtutorial.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class profileController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(profileController.class);

    public profileController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/update-username")
    public ResponseEntity<?> updateUsername(@RequestBody Map<String, String> request, Authentication authentication) {
        String newUsername = request.get("username");
        String currentEmail = authentication.getName();

        logger.info("Received request to update username for user: {}", currentEmail);
        logger.debug("New username: {}", newUsername);

        try {
            userService.updateUsername(currentEmail, newUsername);
            logger.info("Username successfully updated for user: {}", currentEmail);
            return ResponseEntity.ok().body(Map.of("message", "Username updated successfully."));
        } catch (Exception e) {
            logger.error("Error updating username for user: {}", currentEmail, e);
            return ResponseEntity.status(500).body(Map.of("message", "Failed to update username."));
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequest request, Authentication authentication) {
        String username = authentication.getName();
        userService.changePassword(username, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().body(Map.of("message", "Password updated successfully."));
    }
}
