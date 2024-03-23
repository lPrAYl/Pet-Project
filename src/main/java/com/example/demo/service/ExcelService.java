package com.example.demo.service;

import ch.qos.logback.classic.Logger;
import com.example.demo.entity.VacancyEntity;
import com.example.demo.repository.VacancyRepository;
import jakarta.activation.DataSource;
import jakarta.mail.util.ByteArrayDataSource;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ExcelService {

    private final VacancyRepository vacancyRepository;
    Logger log = (Logger) LoggerFactory.getLogger(ExcelService.class);
    private final VacancyService vacancyService;

    public ExcelService(VacancyRepository vacancyRepository, VacancyService vacancyService) {
        this.vacancyRepository = vacancyRepository;
        this.vacancyService = vacancyService;
    }

    public Optional<DataSource> generateExcelFile() {
        //todo удалить перед релизом
//                vacancyService.updateVacancies();
        List<VacancyEntity> vacancyEntityList = vacancyRepository.findAllByIsSentIsFalseOrderByIdDesc();
        if (vacancyEntityList.isEmpty()) {
            return Optional.empty();
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             XSSFWorkbook workbook = new XSSFWorkbook()
        ) {
            Sheet sheet = workbook.createSheet("new");
            fillExcelHeader(sheet);
            fillExcelData(sheet, vacancyEntityList);
            workbook.write(outputStream);
            return Optional.of(new ByteArrayDataSource(outputStream.toByteArray(), "application/vnd.ms-excel"));
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }

        return Optional.empty();
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

    private void fillExcelData(Sheet sheet, List<VacancyEntity> vacancies) {
        int rowNum = 1;
        for (VacancyEntity vacancy : vacancies) {
            Row row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(vacancy.getVacancyName());
            row.createCell(1).setCellValue(vacancy.getEmployerName());
            row.createCell(2).setCellValue(vacancy.getEmployerUrl());
            row.createCell(3).setCellValue(getSalaryValue(vacancy.getSalaryFrom(), vacancy.getSalaryCurrency()));
            row.createCell(4).setCellValue(getSalaryValue(vacancy.getSalaryTo(), vacancy.getSalaryCurrency()));
            row.createCell(5).setCellValue(vacancy.getVacancyUrl());
            rowNum++;
        }
    }

    private String getSalaryValue(int salary, String currency) {
        return salary == 0 ? "" : salary + " " + currency;
    }
}
