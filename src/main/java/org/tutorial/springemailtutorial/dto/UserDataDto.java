package org.tutorial.springemailtutorial.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDataDto {

    private Long userId;
    private Long boardId;
    private int boardPosition;
    private List<MyTaskDto> tasks;
    private List<MyColumnsDto> columns;

}
