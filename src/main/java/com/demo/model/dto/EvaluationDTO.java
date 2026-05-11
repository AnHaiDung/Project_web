package com.demo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EvaluationDTO {
    private Long sessionId;
    private String competencyLevel;
    private String comment;
    private Long equipmentId;
    private Integer quantity;
}
