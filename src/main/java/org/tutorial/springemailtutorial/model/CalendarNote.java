package org.tutorial.springemailtutorial.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalendarNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private String color;

    private String textColor;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String title;

    private LocalTime timeFrom;

    private LocalTime timeTo;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference(value = "user-calender")
    private User user;

    public CalendarNote(LocalDate date, String content, User user) {
        this.date = date;
        this.content = content;
        this.user = user;
    }
}
