package org.tutorial.springemailtutorial.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyTaskDto {

    private Long id;

    private String text;

    private String color;

    private String tagText;

    private String tagColor;

    private Long columnId;

    private int position;

    private String avatarBackgroundColor;
    private String avatarImageUrl;
    private String avatarInitials;
    private String avatarUsername;

}