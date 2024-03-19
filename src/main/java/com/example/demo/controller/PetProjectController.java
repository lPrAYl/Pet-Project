package com.example.demo.controller;

import com.example.demo.service.VacancyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class PetProjectController {

    private final VacancyService vacancyService;

    public PetProjectController(VacancyService vacancyService) {
        this.vacancyService = vacancyService;
    }

    @GetMapping("v")
    public void getAllVacancies() {
        vacancyService.getAllVacancies();
    }
}
