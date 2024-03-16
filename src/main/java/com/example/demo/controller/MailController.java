package com.example.demo.controller;

import com.example.demo.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("\\")
public class MailController {

    @Autowired
    private MailService mailService;

    @GetMapping("mail")
    public void sendMail() {
        mailService.sendSimpleMessage("q1122333@yandex.ru", "subject", "-----text-----");
    }

}
