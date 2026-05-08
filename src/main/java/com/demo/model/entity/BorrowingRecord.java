package com.demo.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "borrowing_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;
    private Date createdAt;
    private Date exportedAt;

    @OneToOne
    @JoinColumn(name = "session_id", nullable = false)
    private MentoringSession session;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @OneToMany(mappedBy = "borrowingRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BorrowingDetail> details = new ArrayList<>();
}
