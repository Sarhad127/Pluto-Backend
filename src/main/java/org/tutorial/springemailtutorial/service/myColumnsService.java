package org.tutorial.springemailtutorial.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tutorial.springemailtutorial.dto.MyColumnsDto;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.model.myColumns;
import org.tutorial.springemailtutorial.repository.MyColumnsRepository;
import org.tutorial.springemailtutorial.repository.UserRepository;

import java.util.Optional;

@Service
public class myColumnsService {

    private final MyColumnsRepository myColumnsRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Autowired
    public myColumnsService(MyColumnsRepository myColumnsRepository,
                            UserRepository userRepository,
                            JwtService jwtService) {
        this.myColumnsRepository = myColumnsRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public myColumns saveColumn(MyColumnsDto columnDto, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found.");
        }
        myColumns column = new myColumns();
        column.setTitle(columnDto.getTitle());
        column.setUser(user.get());
        return myColumnsRepository.save(column);
    }
}

/*
* JWT tokens must be base64url encoded without spaces

The "Bearer " prefix was causing the token parser to fail when it encountered the space

By removing the prefix, we pass only the actual token to the JWT service
* */