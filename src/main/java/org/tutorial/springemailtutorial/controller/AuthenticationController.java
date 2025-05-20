package org.tutorial.springemailtutorial.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.*;
import org.tutorial.springemailtutorial.customExceptions.DuplicateUserException;
import org.tutorial.springemailtutorial.dto.*;
import org.tutorial.springemailtutorial.model.Board;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.model.MyColumn;
import org.tutorial.springemailtutorial.responses.ResponseMessage;
import org.tutorial.springemailtutorial.service.AuthenticationService;
import org.tutorial.springemailtutorial.service.BoardService;
import org.tutorial.springemailtutorial.service.JwtService;
import org.tutorial.springemailtutorial.service.UserService;

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
    private final BoardService boardService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginUserDto loginUserDto) {
        logger.info("Attempting login for user: {}", loginUserDto.getEmail());
        try {
            User authenticatedUser = authenticationService.authenticate(loginUserDto);
            String jwtToken = jwtService.generateToken(authenticatedUser);
            String authHeader = "Bearer " + jwtToken;
            List<BoardDto> userBoards = boardService.getBoards(authHeader);
            if (userBoards == null || userBoards.isEmpty()) {
                userService.createAvatarFromUsername(authenticatedUser.getUsername());
                Board newBoard = boardService.createDefaultBoard(authenticatedUser);
                logger.info("No boards found, created a default board with ID: {}", newBoard.getId());
            }

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

    @PostMapping("/boards/{boardId}/columns")
    public ResponseEntity<?> createColumn(
            @PathVariable Long boardId,
            @RequestBody MyColumnsDto columnDto,
            @RequestHeader("Authorization") String token) {

        logger.info("Creating new column for board {} with title: {}, color: {}",
                boardId, columnDto.getTitle(), columnDto.getTitleColor());

        try {
            if (token == null || !token.startsWith("Bearer ")) {
                logger.warn("Invalid authorization header format");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid authorization header");
            }

            MyColumn createdColumn = myColumnsService.saveColumn(columnDto, boardId, token);
            logger.info("Successfully created column with ID: {}", createdColumn.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createdColumn);

        } catch (RuntimeException e) {
            logger.error("Failed to create column: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error creating column: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create column. Please try again.");
        }
    }

    @PutMapping("/boards/{boardId}/columns/order")
    public ResponseEntity<ResponseMessage> reorderColumns(
            @PathVariable Long boardId,
            @RequestBody List<MyColumnsDto> columnDtos,
            @RequestHeader("Authorization") String authHeader) {

        logger.info("Reordering columns for board {} with {} columns", boardId, columnDtos.size());

        try {
            if (columnDtos == null || columnDtos.isEmpty()) {
                logger.warn("Empty column list provided for reordering");
                return ResponseEntity.badRequest()
                        .body(new ResponseMessage("Column list cannot be empty"));
            }

            myColumnsService.reorderColumns(columnDtos, boardId, authHeader);

            logger.info("Columns reordered successfully for board {}", boardId);
            return ResponseEntity.ok()
                    .body(new ResponseMessage("Columns reordered successfully"));

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage(e.getMessage()));

        } catch (EntityNotFoundException e) {
            logger.warn("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseMessage(e.getMessage()));

        } catch (SecurityException e) {
            logger.warn("Authorization failure: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseMessage(e.getMessage()));

        } catch (Exception e) {
            logger.error("Unexpected error reordering columns: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new ResponseMessage("Failed to reorder columns. Please try again."));
        }
    }

    @PutMapping("/boards/{boardId}/columns/{columnId}")
    public ResponseEntity<?> updateColumn(
            @PathVariable Long boardId,
            @PathVariable Long columnId,
            @RequestBody MyColumnsDto columnDto,
            @RequestHeader("Authorization") String authHeader) {

        logger.info("Updating column {} in board {} with new title: '{}' and color: '{}'",
                columnId, boardId, columnDto.getTitle(), columnDto.getTitleColor());

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Invalid authorization header format");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(("Invalid authorization header"));
            }

            MyColumn updatedColumn = myColumnsService.updateColumn(columnId, boardId, columnDto, authHeader);

            logger.info("Successfully updated column {}", columnId);
            return ResponseEntity.ok(updatedColumn);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body((e.getMessage()));

        } catch (EntityNotFoundException e) {
            logger.warn("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body((e.getMessage()));

        } catch (SecurityException e) {
            logger.warn("Authorization failure: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body((e.getMessage()));

        } catch (Exception e) {
            logger.error("Unexpected error updating column: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(("Failed to update column. Please try again."));
        }
    }

    @DeleteMapping("/boards/{boardId}/columns/{columnId}")
    public ResponseEntity<?> deleteColumn(
            @PathVariable Long boardId,
            @PathVariable Long columnId,
            @RequestHeader("Authorization") String authHeader) {

        logger.info("Attempting to delete column {} from board {}", columnId, boardId);

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Invalid authorization header format");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(("Invalid authorization header"));
            }

            myColumnsService.deleteColumn(columnId, boardId, authHeader);

            logger.info("Successfully deleted column {}", columnId);
            return ResponseEntity.ok()
                    .body(new ResponseMessage("Column deleted successfully"));

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid delete request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body((e.getMessage()));

        } catch (EntityNotFoundException e) {
            logger.warn("Resource not found during deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body((e.getMessage()));

        } catch (SecurityException e) {
            logger.warn("Authorization failure during deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body((e.getMessage()));

        } catch (Exception e) {
            logger.error("Unexpected error deleting column: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(("Failed to delete column. Please try again."));
        }
    }
}
