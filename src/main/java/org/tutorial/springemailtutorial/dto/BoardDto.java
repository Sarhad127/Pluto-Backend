package org.tutorial.springemailtutorial.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardDto {
    private Long id;
    private String title;
    private int position;
    private Long userId;
    private List<MyColumnsDto> columns;

}
