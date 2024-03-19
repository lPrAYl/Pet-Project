package com.example.demo.service;

import com.example.demo.dto.VacancyDto;
import com.example.demo.service.mail.SendEmailsService;
import jakarta.activation.DataSource;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class ExcelService {

    @Value("${prop.mail.send_to}")
    private String sendTo;
    private final SendEmailsService sendEmailsService;

    public ExcelService(SendEmailsService sendEmailsService) {
        this.sendEmailsService = sendEmailsService;
    }

    public void generateExcelFile(Map<Long, VacancyDto> vacancyDtoMap) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             XSSFWorkbook workbook = new XSSFWorkbook()
        ) {
            Sheet sheet = workbook.createSheet("new");
            fillExcelHeader(sheet);
            fillExcelData(vacancyDtoMap, sheet);
            workbook.write(outputStream);
            DataSource attachment = new ByteArrayDataSource(outputStream.toByteArray(), "application/vnd.ms-excel");
            //todo нужно вынести отсюда
            sendEmailsService.sendHtmlMessage(sendTo, "subject", "-----text-----", "file.xlsx", attachment);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    private void fillExcelHeader(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Название вакансии");
        headerRow.createCell(1).setCellValue("Название работодателя");
        headerRow.createCell(2).setCellValue("Сайт работодателя");
        headerRow.createCell(3).setCellValue("Зарплата от");
        headerRow.createCell(4).setCellValue("Зарплата до");
        headerRow.createCell(5).setCellValue("Url вакансии");
    }

    private void fillExcelData(Map<Long, VacancyDto> vacancyDtoMap, Sheet sheet) {
        int rowNum = 1;
        for (VacancyDto vacancyDto : vacancyDtoMap.values()) {
            Row row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(vacancyDto.getName());
            row.createCell(1).setCellValue(vacancyDto.getEmployerName());
            row.createCell(2).setCellValue(vacancyDto.getEmployerUrl());
            row.createCell(3).setCellValue(getSalaryValue(vacancyDto.getSalaryFrom(), vacancyDto.getSalaryCurrency()));
            row.createCell(4).setCellValue(getSalaryValue(vacancyDto.getSalaryTo(), vacancyDto.getSalaryCurrency()));
            row.createCell(5).setCellValue(vacancyDto.getVacancyUrl());
            rowNum++;
        }
    }

    private String getSalaryValue(int salary, String currency) {
        if (salary == 0) {
            return "";
        }
        return salary + " " + currency;
    }
}
