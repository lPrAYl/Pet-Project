package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class VacancyDto {

    private Long id;
    private String name;
    private Long employerId;
    private String employerName;
    private String employerUrl;
    private Integer salaryFrom;
    private Integer salaryTo;
    private String salaryCurrency;
}
