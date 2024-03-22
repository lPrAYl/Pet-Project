package com.example.demo.controller;

import com.example.demo.service.VacancyService;
import com.example.demo.service.mail.EmailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class PetProjectController {

    private final EmailService emailService;

    public PetProjectController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("email")
    public void sendEmailWithVacancies() {
        emailService.sendEmail();
    }
}
