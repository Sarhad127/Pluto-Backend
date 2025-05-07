package org.tutorial.springemailtutorial.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CalendarNoteDTO {

    private LocalDate date;
    private String content;
}
