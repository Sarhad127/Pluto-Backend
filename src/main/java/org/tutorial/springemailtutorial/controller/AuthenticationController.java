package org.tutorial.springemailtutorial.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.tutorial.springemailtutorial.dto.LoginUserDto;
import org.tutorial.springemailtutorial.dto.RegisterUserDto;
import org.tutorial.springemailtutorial.dto.VerifyUserDto;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.service.AuthenticationService;
import org.tutorial.springemailtutorial.service.JwtService;

@Controller
@RequestMapping("/auth")
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        logger.info("Displaying login page");
        model.addAttribute("loginUserDto", new LoginUserDto());
        return "login";
    }

    @PostMapping("/login")
    public String authenticate(@ModelAttribute LoginUserDto loginUserDto, Model model) {
        logger.info("Attempting login for user: {}", loginUserDto.getEmail());
        try {
            User authenticatedUser = authenticationService.authenticate(loginUserDto);
            logger.info("Authentication successful for user: {}", authenticatedUser.getEmail());
            String jwtToken = jwtService.generateToken(authenticatedUser);
            model.addAttribute("token", jwtToken);
            return "redirect:/dashboard";
        } catch (Exception e) {
            logger.error("Authentication failed for user: {} - Error: {}", loginUserDto.getEmail(), e.getMessage());
            model.addAttribute("error", "Invalid credentials");
            return "login";
        }
    }

    @GetMapping("/signup")
    public String registerPage(Model model) {
        logger.info("Displaying signup page");
        model.addAttribute("registerUserDto", new RegisterUserDto());
        return "register";
    }

    @PostMapping("/signup")
    public String register(@ModelAttribute RegisterUserDto registerUserDto, Model model) {
        logger.info("Attempting registration for user: {}", registerUserDto.getEmail());
        try {
            authenticationService.signup(registerUserDto);
            logger.info("Registration successful for user: {}", registerUserDto.getEmail());
            model.addAttribute("message", "Registration successful! Please check your email to verify your account.");
            return "redirect:/auth/verify-email?email=" + registerUserDto.getEmail();
        } catch (Exception e) {
            logger.error("Registration failed for user: {} - Error: {}", registerUserDto.getEmail(), e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/verify-email")
    public String verifyPage(@RequestParam String email, Model model) {
        logger.info("Displaying verification page for email: {}", email);
        model.addAttribute("email", email);
        return "verify";
    }

    @PostMapping("/verify")
    public String verifyUser(@RequestParam String email, @RequestParam String verificationCode, Model model) {
        logger.info("Attempting to verify user: {} with verification code: {}", email, verificationCode);
        try {
            authenticationService.verifyUser(new VerifyUserDto(email, verificationCode));
            logger.info("Account verified successfully for user: {}", email);
            model.addAttribute("message", "Account verified successfully! You can now log in.");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            logger.error("Account verification failed for user: {} - Error: {}", email, e.getMessage());
            model.addAttribute("error", "Invalid verification code. Please try again.");
            return "verify";
        }
    }

    @PostMapping("/resend")
    @ResponseBody
    public String resendVerificationCode(@RequestParam String email) {
        logger.info("Resending verification code for email: {}", email);
        try {
            authenticationService.resendVerificationCode(email);
            logger.info("Verification code resent to email: {}", email);
            return "Verification code sent.";
        } catch (RuntimeException e) {
            logger.error("Failed to resend verification code to email: {} - Error: {}", email, e.getMessage());
            return e.getMessage();
        }
    }
}
