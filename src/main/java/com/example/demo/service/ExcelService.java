package com.example.demo.service;

import jakarta.activation.DataSource;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Service
public class ExcelService {

    @Value("${prop.mail.send_to}")
    private String sendTo;
    private final MailService mailService;

    public ExcelService(MailService mailService) {
        this.mailService = mailService;
    }

    public void generateExcelFile() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("new");
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("это первая строка");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }

        DataSource attachment = new ByteArrayDataSource(outputStream.toByteArray(), "application/vnd.ms-excel");
        mailService.sendHtmlMessage(sendTo, "subject", "-----text-----", "file.xlsx", attachment);
    }
}
