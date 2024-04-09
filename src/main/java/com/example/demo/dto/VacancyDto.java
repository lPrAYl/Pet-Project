package com.example.demo.dto;

public class VacancyDto {
    private Long id;
    private String vacancyName = "";
    //  alternate_url вакансии на hh.ru
    private String vacancyUrl;
    private Long employerId;
    private String employerName = "";
    //  url работодателя на hh.ru
    private String employerUrl = "";
    //  url работодателя на hh.ru
    private String employerHHUrl;
    private Integer salaryFrom = 0;
    private Integer salaryTo = 0;
    private String salaryCurrency = "";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVacancyName() {
        return vacancyName;
    }

    public void setVacancyName(String vacancyName) {
        this.vacancyName = vacancyName;
    }

    public Long getEmployerId() {
        return employerId;
    }

    public void setEmployerId(Long employerId) {
        this.employerId = employerId;
    }

    public String getEmployerName() {
        return employerName;
    }

    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }

    public String getEmployerUrl() {
        return employerUrl;
    }

    public void setEmployerUrl(String employerUrl) {
        this.employerUrl = employerUrl;
    }

    public String getEmployerHHUrl() {
        return employerHHUrl;
    }

    public void setEmployerHHUrl(String employerHHUrl) {
        this.employerHHUrl = employerHHUrl;
    }

    public Integer getSalaryFrom() {
        return salaryFrom;
    }

    public void setSalaryFrom(Integer salaryFrom) {
        this.salaryFrom = salaryFrom;
    }

    public Integer getSalaryTo() {
        return salaryTo;
    }

    public void setSalaryTo(Integer salaryTo) {
        this.salaryTo = salaryTo;
    }

    public String getSalaryCurrency() {
        return salaryCurrency;
    }

    public void setSalaryCurrency(String salaryCurrency) {
        this.salaryCurrency = salaryCurrency;
    }

    public String getVacancyUrl() {
        return vacancyUrl;
    }

    public void setVacancyUrl(String vacancyUrl) {
        this.vacancyUrl = vacancyUrl;
    }

}
