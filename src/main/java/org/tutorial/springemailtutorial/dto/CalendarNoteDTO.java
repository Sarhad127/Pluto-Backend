package org.tutorial.springemailtutorial.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CalendarNoteDTO {

    private LocalDate date;
    private String content;
    private String color;
    private String textColor;
    private String title;
    private LocalTime timeFrom;
    private LocalTime timeTo;
}
