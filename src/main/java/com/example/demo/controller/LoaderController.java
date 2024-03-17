package com.example.demo.controller;

import com.example.demo.service.ExcelService;
import com.example.demo.service.mail.SendEmailsService;
import com.example.demo.service.mail.ReceiveEmailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("\\")
public class LoaderController {

    @Autowired
    private SendEmailsService sendEmailsService;
    @Autowired
    private ExcelService excelService;
    @Autowired
    private ReceiveEmailsService receiveEmailsService;

    @GetMapping("mail")
    public void sendMail() {
        excelService.generateExcelFile();
//    receiveEmailsService.receiveEmails();
    }

}
