package com.example.demo.service.mail;

import ch.qos.logback.classic.Logger;
import com.example.demo.service.ExcelService;
import com.example.demo.service.VacancyService;
import jakarta.activation.DataSource;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmailService {

    Logger log = (Logger) LoggerFactory.getLogger(EmailService.class);

    @Value("${spring.mail.username}")
    private String from;
    @Value("${prop.mail.send_to}")
    private String sendTo;
    @Value("${prop.mail.send_copy}")
    private String sendCopy;
    private final JavaMailSender javaMailSender;
    private final ExcelService excelService;

    public EmailService(JavaMailSender javaMailSender, ExcelService excelService, VacancyService vacancyService) {
        this.javaMailSender = javaMailSender;
        this.excelService = excelService;
    }

    public void sendEmail() {
        excelService.generateExcelFile().ifPresentOrElse(this::sendHtmlMessage, this::sendSimpleMessage);
    }

    private void sendSimpleMessage() {
        String logText = "письмо на адрес: " + sendTo + ", копия: " + sendCopy;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(sendTo);
        message.setCc(sendCopy);
        message.setFrom(from);
        message.setSubject("Нет вакансии за прошлые сутки");
        message.setText("Нет вакансии за прошлые сутки");
        try {
            javaMailSender.send(message);
            log.info("(sendSimpleMessage()) Отправлено " + logText);
        } catch (Exception e) {
            log.error("(sendSimpleMessage()) Не отправлено " + logText);
            log.error(e.getLocalizedMessage(), e);
        }
    }

    private void sendHtmlMessage(DataSource attachment) {
        String logText = "письмо на адрес: " + sendTo + ", копия: " + sendCopy;
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(sendTo);
            helper.setCc(sendCopy);
            helper.setFrom(from);
            helper.addAttachment("file.xlsx", attachment);
            helper.setSubject("Новые вакансии за " + LocalDate.now().minusDays(1));
            helper.setText("Новые вакансии за " + LocalDate.now().minusDays(1), true);
            javaMailSender.send(message);
            log.info("(sendHtmlMessage()) Отправлено " + logText);
        } catch (Exception e) {
            log.error("(sendHtmlMessage()) Не отправлено " + logText);
            log.error(e.getLocalizedMessage(), e);
        }
    }
}
