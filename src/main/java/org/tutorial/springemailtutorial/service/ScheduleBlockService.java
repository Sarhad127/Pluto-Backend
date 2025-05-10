package org.tutorial.springemailtutorial.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tutorial.springemailtutorial.dto.ScheduleBlockDto;
import org.tutorial.springemailtutorial.model.ScheduleBlock;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.repository.ScheduleBlockRepository;
import org.tutorial.springemailtutorial.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleBlockService {

    private final ScheduleBlockRepository scheduleBlockRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Autowired
    public ScheduleBlockService(ScheduleBlockRepository scheduleBlockRepository,
                                UserRepository userRepository,
                                JwtService jwtService) {
        this.scheduleBlockRepository = scheduleBlockRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    private String getUsernameFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        return jwtService.extractUsername(token);
    }

    public List<ScheduleBlockDto> getScheduleBlocks(String authHeader) {
        String username = getUsernameFromToken(authHeader);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<ScheduleBlock> scheduleBlocks = scheduleBlockRepository.findByUserId(user.getId());
        return scheduleBlocks.stream()
                .map(block -> new ScheduleBlockDto(
                        block.getId(),
                        block.getDay(),
                        block.getStartHour(),
                        block.getEndHour(),
                        block.getTitle(),
                        block.getLabel(),
                        block.getColor(),
                        block.getUser() != null ? block.getUser().getId() : null
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduleBlockDto createScheduleBlock(ScheduleBlockDto dto, String authHeader) {
        String username = getUsernameFromToken(authHeader);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        ScheduleBlock block = new ScheduleBlock();
        block.setDay(dto.getDay());
        block.setStartHour(dto.getStartHour());
        block.setEndHour(dto.getEndHour());
        block.setTitle(dto.getTitle());
        block.setLabel(dto.getLabel());
        block.setColor(dto.getColor());
        block.setUser(user);

        ScheduleBlock savedBlock = scheduleBlockRepository.save(block);

        return new ScheduleBlockDto(
                savedBlock.getId(),
                savedBlock.getDay(),
                savedBlock.getStartHour(),
                savedBlock.getEndHour(),
                savedBlock.getTitle(),
                savedBlock.getLabel(),
                savedBlock.getColor(),
                savedBlock.getUser() != null ? block.getUser().getId() : null
        );
    }

    @Transactional
    public ScheduleBlockDto updateScheduleBlock(Long id, ScheduleBlockDto dto, String authHeader) {
        String username = getUsernameFromToken(authHeader);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        ScheduleBlock block = scheduleBlockRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Schedule Block not found"));

        if (!block.getUser().equals(user)) {
            throw new SecurityException("User not authorized to modify this schedule block");
        }

        if (dto.getDay() != null) {
            block.setDay(dto.getDay());
        }
        if (dto.getStartHour() != null) {
            block.setStartHour(dto.getStartHour());
        }
        if (dto.getEndHour() != null) {
            block.setEndHour(dto.getEndHour());
        }
        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            block.setTitle(dto.getTitle());
        }
        if (dto.getLabel() != null && !dto.getLabel().isBlank()) {
            block.setLabel(dto.getLabel());
        }
        if (dto.getColor() != null && !dto.getColor().isBlank()) {
            block.setColor(dto.getColor());
        }

        return new ScheduleBlockDto(
                block.getId(),
                block.getDay(),
                block.getStartHour(),
                block.getEndHour(),
                block.getTitle(),
                block.getLabel(),
                block.getColor(),
                block.getUser() != null ? block.getUser().getId() : null
        );
    }

    @Transactional
    public void deleteScheduleBlock(Long id, String authHeader) {
        String username = getUsernameFromToken(authHeader);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        ScheduleBlock block = scheduleBlockRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Schedule Block not found"));

        if (!block.getUser().equals(user)) {
            throw new SecurityException("User not authorized to delete this schedule block");
        }

        scheduleBlockRepository.delete(block);
    }
}