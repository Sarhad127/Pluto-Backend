package org.tutorial.springemailtutorial.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    private String color;

    private String tagText;

    private String tagColor;

    private int position;

    private String avatarBackgroundColor;
    @Column(name = "avatar_image_url", length = 1024)
    private String avatarImageUrl;
    private String avatarInitials;
    private String avatarUsername;

    @ManyToOne
    @JoinColumn(name = "column_id")
    @JsonBackReference
    private MyColumn column;
}
