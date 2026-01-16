package org.tutorial.springemailtutorial.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyColumnsDto {

    private Long id;
    private String title;
    private Integer placement;
    private String titleColor;
    private List<MyTaskDto> tasks;
}
