package org.tutorial.springemailtutorial.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tutorial.springemailtutorial.dto.MyColumnsDto;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.model.myColumns;
import org.tutorial.springemailtutorial.repository.MyColumnsRepository;
import org.tutorial.springemailtutorial.repository.UserRepository;

import java.util.List;
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
        String token = authHeader.substring(7).trim();
        String username = jwtService.extractUsername(token);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found.");
        }
        List<myColumns> userColumns = myColumnsRepository.findByUserIdOrderByPlacement(user.get().getId());
        int placement = userColumns.size() + 1;
        myColumns column = new myColumns();
        column.setTitle(columnDto.getTitle());
        column.setTitleColor(columnDto.getTitleColor());
        column.setUser(user.get());
        column.setPlacement(placement);
        return myColumnsRepository.save(column);
    }

    @Transactional
    public void reorderColumns(List<MyColumnsDto> columnDtos, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        String username = jwtService.extractUsername(token);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found.");
        }
        List<myColumns> columns = myColumnsRepository.findByUserIdOrderByPlacement(user.get().getId());
        for (int i = 0; i < columnDtos.size(); i++) {
            MyColumnsDto dto = columnDtos.get(i);
            myColumns column = columns.stream()
                    .filter(c -> c.getTitle().equals(dto.getTitle()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Column not found"));

            column.setPlacement(i + 1);

            if (dto.getTitleColor() != null) {
                column.setTitleColor(dto.getTitleColor());
            }
            myColumnsRepository.save(column);
        }
    }

    @Transactional
    public myColumns updateColumn(Long columnId, MyColumnsDto columnDto, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }

        String token = authHeader.substring(7).trim();
        String username = jwtService.extractUsername(token);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found.");
        }

        myColumns column = myColumnsRepository.findById(columnId)
                .orElseThrow(() -> new RuntimeException("Column not found"));

        if (!column.getUser().getId().equals(user.get().getId())) {
            throw new RuntimeException("User not authorized to edit this column.");
        }

        column.setTitle(columnDto.getTitle());
        column.setTitleColor(columnDto.getTitleColor());

        return myColumnsRepository.save(column);
    }

}