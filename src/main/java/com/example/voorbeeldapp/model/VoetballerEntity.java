package com.example.voorbeeldapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "voetballers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoetballerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String naam;

    @Column(nullable = false)
    private String positie;

    @Column(nullable = false)
    private String team;
}
