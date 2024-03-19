package com.example.demo.dto;

import lombok.Data;

@Data
public class VacancyDto {
    private Long id;
    private String name = "";
    private Long employerId;
    private String employerName = "";
    private String employerUrl = "";
    private Integer salaryFrom = 0;
    private Integer salaryTo = 0;
    private String salaryCurrency = "";
    private String vacancyUrl;
}
