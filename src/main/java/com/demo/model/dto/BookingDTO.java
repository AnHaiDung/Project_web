package com.demo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BookingDTO {
    private Long departmentId;
    private Long lecturerId;
    private String topic;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private Date startTime;
}
