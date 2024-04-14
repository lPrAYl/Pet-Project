package com.example.demo.controller;

import com.example.demo.service.mail.EmailService;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("start")
    public String getString(){
        return "Это ответ от сервера";
    }


    @GetMapping("/login")
    public String loginEndpoint() {
        return "Login!";
    }

    @GetMapping("/admin")
    public String adminEndpoint() {
        return "Admin!";
    }

    @GetMapping("/user")
    public String userEndpoint() {
        return "User!";
    }

    @GetMapping("/all")
    public String allRolesEndpoint() {
        return "All Roles!";
    }

    @DeleteMapping("/delete")
    public String deleteEndpoint(@RequestBody String s) {
        return "I am deleting " + s;
    }
}
