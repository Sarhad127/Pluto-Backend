package org.tutorial.springemailtutorial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tutorial.springemailtutorial.model.ScheduleSettings;

import java.util.Optional;

public interface ScheduleSettingsRepository extends JpaRepository<ScheduleSettings, Long> {
    Optional<ScheduleSettings> findByUserId(Long userId);
}
