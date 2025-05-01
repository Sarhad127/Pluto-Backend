package org.tutorial.springemailtutorial.service;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.repository.UserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(OAuth2Service.class);

    public String registerOrUpdateOAuth2User(OAuth2User oAuth2User, String provider) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        logger.info("{} OAuth2 Attributes: {}", provider, attributes);

        try {
            String username = extractUsername(attributes, provider);
            String email = extractEmail(attributes, provider, username);

            logger.info("Processing {} user - Email: {}, Username: {}", provider, email, username);

            Optional<User> existingUser = userRepository.findByEmail(email);

            User user = existingUser.orElseGet(() -> {
                User newUser = new User();
                newUser.setEnabled(true);
                newUser.setProvider(provider);
                newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                logger.info("Creating new user for {}", email);
                return newUser;
            });

            user.setUsername(username);
            user.setEmail(email);
            user.setProvider(provider);

            userRepository.save(user);
            logger.info("Successfully saved {} user: {}", provider, user);

            return email;
        } catch (Exception e) {
            logger.error("Error processing {} OAuth user: {}", provider, e.getMessage());
            throw e;
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