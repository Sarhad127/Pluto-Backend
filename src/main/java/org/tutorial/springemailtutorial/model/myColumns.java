package org.tutorial.springemailtutorial.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class myColumns {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private Integer placement;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
