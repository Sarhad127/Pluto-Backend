package org.tutorial.springemailtutorial.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tutorial.springemailtutorial.dto.ScheduleBlockDto;
import org.tutorial.springemailtutorial.dto.ScheduleSettingsDto;
import org.tutorial.springemailtutorial.service.ScheduleBlockService;

import java.util.List;

@RestController
@RequestMapping("/api/schedule-blocks")
@RequiredArgsConstructor
public class ScheduleBlockController {

    private final ScheduleBlockService scheduleBlockService;

    @GetMapping
    public ResponseEntity<List<ScheduleBlockDto>> getScheduleBlocks(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(scheduleBlockService.getScheduleBlocks(authHeader));
    }

    @PostMapping
    public ResponseEntity<ScheduleBlockDto> createScheduleBlock(@RequestBody ScheduleBlockDto dto,
                                                                @RequestHeader("Authorization") String authHeader) {
        System.out.println("dto: " + dto);
        return ResponseEntity.ok(scheduleBlockService.createScheduleBlock(dto, authHeader));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleBlockDto> updateScheduleBlock(@PathVariable Long id,
                                                                @RequestBody ScheduleBlockDto dto,
                                                                @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(scheduleBlockService.updateScheduleBlock(id, dto, authHeader));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScheduleBlock(@PathVariable Long id,
                                                    @RequestHeader("Authorization") String authHeader) {
        scheduleBlockService.deleteScheduleBlock(id, authHeader);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/schedule-settings")
    public ResponseEntity<String> updateScheduleSettings(@RequestBody ScheduleSettingsDto settingsDto,
                                                         @RequestHeader("Authorization") String authHeader) {
        if (settingsDto.getStartHour() < 0 || settingsDto.getEndHour() > 23 || settingsDto.getStartHour() >= settingsDto.getEndHour()) {
            return ResponseEntity.badRequest().body("Invalid start or end hour");
        }
        scheduleBlockService.updateScheduleSettings(settingsDto, authHeader);
        return ResponseEntity.ok("Schedule hours updated successfully");
    }

    @GetMapping("/schedule-settings")
    public ResponseEntity<ScheduleSettingsDto> getScheduleSettings(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(scheduleBlockService.getScheduleSettings(authHeader));
    }
}
