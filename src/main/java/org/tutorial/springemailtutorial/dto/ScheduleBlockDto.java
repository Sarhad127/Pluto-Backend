package org.tutorial.springemailtutorial.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tutorial.springemailtutorial.model.ScheduleBlock;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleBlockDto {

    private Long id;
    private ScheduleBlock.DayOfWeek day;
    private Integer startHour;
    private Integer endHour;
    private String title;
    private String label;
    private String color;
    private Long userId;
}
