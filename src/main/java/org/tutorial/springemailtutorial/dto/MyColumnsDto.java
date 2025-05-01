package org.tutorial.springemailtutorial.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyColumnsDto {

    private Long id;
    private String title;
    private Integer placement;
    private String titleColor;
}
