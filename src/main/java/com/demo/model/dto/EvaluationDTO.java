package com.demo.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvaluationDTO {
    private Long sessionId;
    private String competencyLevel;
    private String comment;
    private Long equipmentId;
    private Integer quantity;
}
