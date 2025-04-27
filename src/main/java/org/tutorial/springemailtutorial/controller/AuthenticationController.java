package org.tutorial.springemailtutorial.controller;

import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.*;
import org.tutorial.springemailtutorial.customExceptions.DuplicateUserException;
import org.tutorial.springemailtutorial.dto.LoginUserDto;
import org.tutorial.springemailtutorial.dto.MyColumnsDto;
import org.tutorial.springemailtutorial.dto.RegisterUserDto;
import org.tutorial.springemailtutorial.dto.VerifyUserDto;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.model.myColumns;
import org.tutorial.springemailtutorial.responses.ResponseMessage;
import org.tutorial.springemailtutorial.service.AuthenticationService;
import org.tutorial.springemailtutorial.service.JwtService;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final org.tutorial.springemailtutorial.service.myColumnsService myColumnsService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginUserDto loginUserDto) {
        logger.info("Attempting login for user: {}", loginUserDto.getEmail());
        try {
            User authenticatedUser = authenticationService.authenticate(loginUserDto);

            String jwtToken = jwtService.generateToken(authenticatedUser);
            return ResponseEntity.ok().body(Collections.singletonMap("token", jwtToken));

        } catch (DisabledException e) {
            logger.warn("User {} is not verified. Triggering re-verification.", loginUserDto.getEmail());
            try {
                authenticationService.resendVerificationCode(loginUserDto.getEmail());
                return ResponseEntity.status(400).body(
                        Collections.singletonMap("error", "UNVERIFIED_USER")
                );
            } catch (Exception ex) {
                logger.error("Failed to resend verification code: {}", ex.getMessage());
                return ResponseEntity.status(500).body("Failed to resend verification code. Please try again.");
            }
        } catch (Exception e) {
            logger.error("Authentication failed for user: {} - Error: {}", loginUserDto.getEmail(), e.getMessage());
            return ResponseEntity.status(400).body("Invalid credentials");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody RegisterUserDto registerUserDto) {
        logger.info("Attempting registration for user: {}", registerUserDto.getEmail());
        try {
            authenticationService.signup(registerUserDto);
            logger.info("Registration successful for user: {}", registerUserDto.getEmail());
            return ResponseEntity.ok().body("Registration successful! Please check your email to verify your account.");
        } catch (DuplicateUserException e) {
            logger.error("Registration failed for user: {} - Error: {}", registerUserDto.getEmail(), e.getMessage());
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Registration failed for user: {} - Error: {}", registerUserDto.getEmail(), e.getMessage());
            return ResponseEntity.status(400).body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam String email, @RequestParam String verificationCode) {
        logger.info("Attempting to verify user: {} with verification code: {}", email, verificationCode);
        try {
            authenticationService.verifyUser(new VerifyUserDto(email, verificationCode));
            logger.info("Account verified successfully for user: {}", email);
            return ResponseEntity.ok().body("Account verified successfully! You can now log in.");
        } catch (RuntimeException e) {
            logger.error("Account verification failed for user: {} - Error: {}", email, e.getMessage());
            return ResponseEntity.status(400).body("Invalid verification code. Please try again.");
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email) {
        logger.info("Resending verification code for email: {}", email);
        try {
            authenticationService.resendVerificationCode(email);
            logger.info("Verification code resent to email: {}", email);
            return ResponseEntity.ok().body("Verification code sent.");
        } catch (RuntimeException e) {
            logger.error("Failed to resend verification code to email: {} - Error: {}", email, e.getMessage());
            return ResponseEntity.status(500).body("Failed to resend verification code.");
        }
    }

    @PostMapping("/columns")
    public ResponseEntity<?> createColumn(@RequestBody MyColumnsDto columnDto,
                                          @RequestHeader("Authorization") String token) {
        logger.info("Creating a new column with title: {}, color: {}", columnDto.getTitle(), columnDto.getTitleColor());
        logger.info("Authorization header: {}", token);
        try {
            myColumns createdColumn = myColumnsService.saveColumn(columnDto, token);
            return ResponseEntity.status(201).body(createdColumn);
        } catch (Exception e) {
            logger.error("Failed to create column: {}", e.getMessage());
            return ResponseEntity.status(500).body("Failed to create column. Please try again.");
        }
    }

    @PostMapping("/columns/reorder")
    public ResponseEntity<?> reorderColumns(@RequestBody List<MyColumnsDto> columnDtos,
                                            @RequestHeader("Authorization") String token) {
        logger.info("Reordering columns for user with token: {}", token);
        try {
            myColumnsService.reorderColumns(columnDtos, token);
            return ResponseEntity.status(200).body(new ResponseMessage("Columns reordered successfully."));
        } catch (Exception e) {
            logger.error("Failed to reorder columns: {}", e.getMessage());
            return ResponseEntity.status(500).body(new ResponseMessage("Failed to reorder columns. Please try again."));
        }
    }

    @PutMapping("/columns/{id}")
    public ResponseEntity<?> updateColumn(@PathVariable Long id,
                                          @RequestBody MyColumnsDto columnDto,
                                          @RequestHeader("Authorization") String token) {
        logger.info("Updating column with id: {}, title: {}, color: {}", id, columnDto.getTitle(), columnDto.getTitleColor());
        logger.info("Authorization header: {}", token);
        try {
            myColumns updatedColumn = myColumnsService.updateColumn(id, columnDto, token);
            return ResponseEntity.status(200).body(updatedColumn);
        } catch (Exception e) {
            logger.error("Failed to update column: {}", e.getMessage());
            return ResponseEntity.status(500).body("Failed to update column. Please try again.");
        }
    }

}
