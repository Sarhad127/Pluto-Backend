package org.tutorial.springemailtutorial.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tutorial.springemailtutorial.model.ScheduleSettings;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleSettingsDto {
    private int startHour;
    private int endHour;

    public ScheduleSettingsDto(ScheduleSettings scheduleSettings) {
        this.startHour = scheduleSettings.getStartHour();
        this.endHour = scheduleSettings.getEndHour();
    }
}
