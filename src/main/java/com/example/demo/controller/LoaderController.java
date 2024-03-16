package com.example.demo.controller;

import com.example.demo.service.ExcelService;
import com.example.demo.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("\\")
public class LoaderController {

    @Autowired
    private MailService mailService;
    @Autowired
    private ExcelService excelService;

    @GetMapping("mail")
    public void sendMail() {
        excelService.generateExcelFile();
    }

}
