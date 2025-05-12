package com.example.voorbeeldapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "voetballers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VoetballerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String naam;

    private String positie;
}
