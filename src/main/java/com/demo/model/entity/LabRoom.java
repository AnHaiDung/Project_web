package com.demo.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lab_rooms")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LabRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roomName;
}