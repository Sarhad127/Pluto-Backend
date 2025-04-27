package org.tutorial.springemailtutorial.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyTaskDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("text")
    private String text;

    @JsonProperty("color")
    private String color;

    @JsonProperty("columnId")
    private Long columnId;

    @JsonProperty("position")
    private int position;
}