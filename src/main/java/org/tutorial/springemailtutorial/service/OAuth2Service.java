package org.tutorial.springemailtutorial.service;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.tutorial.springemailtutorial.model.Board;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.repository.BoardRepository;
import org.tutorial.springemailtutorial.repository.UserRepository;

import java.util.*;

@Service
@AllArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BoardRepository boardRepository;

    private static final Logger logger = LoggerFactory.getLogger(OAuth2Service.class);

    public String registerOrUpdateOAuth2User(OAuth2User oAuth2User, String provider) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        logger.info("{} OAuth2 Attributes: {}", provider, attributes);

        try {
            String username = extractUsername(attributes, provider);
            String email = extractEmail(attributes, provider, username);
            logger.info("Processing {} user - Email: {}, Username: {}", provider, email, username);
            Optional<User> existingUser = userRepository.findByEmail(email);
            User user;
            if (existingUser.isEmpty()) {
                user = new User();
                user.setEnabled(true);
                user.setProvider(provider);
                user.setUsername(username);
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                user.setAvatarInitials(generateAvatarInitialsFromUsername(username));
                logger.info("Creating new user for {}", email);
            } else {
                user = existingUser.get();
                logger.info("User already exists: {}", email);
            }

            userRepository.save(user);

            List<Board> existingBoards = boardRepository.findByUsersContaining(user);
            if (existingBoards.isEmpty()) {
                Board defaultBoard = new Board();
                defaultBoard.setTitle("Board 1");
                defaultBoard.setPosition(1);
                defaultBoard.setUsers(new HashSet<>());
                defaultBoard.getUsers().add(user);
                boardRepository.save(defaultBoard);
                logger.info("Created default board for user: {} with ID: {}", user.getEmail(), defaultBoard.getId());
            }
            return email;
        } catch (Exception e) {
            logger.error("Error processing {} OAuth user: {}", provider, e.getMessage());
            throw e;
        }
    }

    private String generateAvatarInitialsFromUsername(String username) {
        if (username == null || username.isBlank()) return "";
        String[] parts = username.split("[^a-zA-Z0-9]+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        } else {
            StringBuilder initials = new StringBuilder();
            for (int i = 0; i < Math.min(2, parts.length); i++) {
                if (!parts[i].isEmpty()) {
                    initials.append(parts[i].charAt(0));
                }
            }
            return initials.toString().toUpperCase();
        }
    }

    private String extractUsername(Map<String, Object> attributes, String provider) {
        return switch (provider.toLowerCase()) {
            case "github" -> (String) attributes.get("login");
            case "google" -> {

                String name = (String) attributes.get("name");
                if (name == null) {
                    name = attributes.get("given_name") + " " +
                            attributes.get("family_name");
                }
                yield name;
            }
            default -> (String) attributes.getOrDefault("name",
                    attributes.getOrDefault("login", "unknown"));
        };
    }

    private String extractEmail(Map<String, Object> attributes, String provider, String username) {
        String email = (String) attributes.get("email");

        if (email == null && "github".equalsIgnoreCase(provider)) {
            email = username + "@github.com";
            logger.info("Generated GitHub email: {}", email);
        }

        if (email == null) {
            logger.error("Email not found in attributes. Available keys: {}", attributes.keySet());
            throw new IllegalArgumentException(
                    "Could not extract email from OAuth2 provider: " + provider +
                            ". Available attributes: " + attributes.keySet());
        }

        return email;
    }
}