package com.example.demo.service;

import jakarta.activation.DataSource;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Slf4j
@Service
public class MailService {

    @Value("${spring.mail.username}")
    private String from;
    @Value("${prop.mail.send_to}")
    private String sendTo;
    private final JavaMailSender javaMailSender;

    public MailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendSimpleMessage(String to, String subject, String text, ByteArrayOutputStream attachment) {
        String logText = "письмо на адрес : " + to + "\nтема:\n" + subject + "\nтекст:\n" + text;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(from);
        message.setSubject(subject);
        message.setText(text);
        try {
            javaMailSender.send(message);
            log.info("Отправлено " + logText);
        } catch (Exception e) {
            log.error("Не отправлено " + logText);
            log.error(e.getLocalizedMessage(), e);
        }
    }

    public void sendHtmlMessage(String to, String subject, String htmlBody, String fileName, DataSource attachment) {
        String logText = "письмо на адрес\n" + to + "\nс темой\n" + subject + "\nи текстом\n" + htmlBody;
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setFrom(from);
            helper.addAttachment(fileName, attachment);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            javaMailSender.send(message);
            log.info("Отправлено " + logText);
        } catch (Exception e) {
            log.error("Не отправлено " + logText);
            log.error(e.getLocalizedMessage(), e);
        }
    }

}
