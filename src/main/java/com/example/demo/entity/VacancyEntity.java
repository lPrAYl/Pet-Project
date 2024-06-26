package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vacancy",
        indexes = {
                @Index(name = "is_send_inx", columnList = "isSent"),
                @Index(name = "vacancies_id_inx", columnList = "vacancyId", unique = true)
        })
public class VacancyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long vacancyId;
    @Column(name = "vacancy_name")
    private String vacancyName;
    //  alternate_url вакансии на hh.ru
    private String vacancyUrl;
    private Long employerId;
    private String employerName;
    //  url работодателя
    private String employerUrl;
    private Integer salaryFrom;
    private Integer salaryTo;
    private String salaryCurrency;
    private Boolean isSent = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVacancyId() {
        return vacancyId;
    }

    public void setVacancyId(Long vacancyId) {
        this.vacancyId = vacancyId;
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

    public Boolean getSent() {
        return isSent;
    }

    public void setSent(Boolean sent) {
        isSent = sent;
    }
}
