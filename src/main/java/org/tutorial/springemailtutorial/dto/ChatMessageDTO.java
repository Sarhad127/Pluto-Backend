package org.tutorial.springemailtutorial.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {
    private String message;
    private String sender;
    private String timestamp;
}
