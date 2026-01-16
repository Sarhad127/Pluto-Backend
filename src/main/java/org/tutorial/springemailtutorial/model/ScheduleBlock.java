package org.tutorial.springemailtutorial.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "schedule_blocks")
@Data
public class ScheduleBlock {

    public enum DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;

        @JsonCreator
        public static DayOfWeek fromString(String value) {
            for (DayOfWeek day : DayOfWeek.values()) {
                if (day.name().equalsIgnoreCase(value)) {
                    return day;
                }
            }
            throw new IllegalArgumentException("Invalid day of week: " + value);
        }

        @JsonValue
        public String toJson() {
            return this.name();
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DayOfWeek day;

    private Integer startHour;

    private Integer endHour;

    private String title;

    private String label;

    @Column(nullable = false)
    private String color = "#f3f3f3";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private User user;
}