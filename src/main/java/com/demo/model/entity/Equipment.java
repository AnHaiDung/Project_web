package com.demo.model.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "equipments")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private int quantity;
}
