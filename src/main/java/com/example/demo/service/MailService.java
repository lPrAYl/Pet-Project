package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MailService {

    @Value("${spring.mail.username}")
    private String from;
    private final JavaMailSender javaMailSender;

    public MailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendSimpleMessage(String to, String subject, String text) {
        String logText = "письмо на адрес\n" + to + "\nс темой\n" + subject + "\nи текстом\n" + text;
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
            e.printStackTrace();
        }
    }

    //	public void sendHtmlMessage(String to, String subject, String htmlBody) throws MessagingException {
    //		MimeMessage message = javaMailSender.createMimeMessage();
    //		MimeMessageHelper helper = new MimeMessageHelper(message, true);
    //		helper.setTo(to);
    //		helper.setSubject(subject);
    //		helper.setText(htmlBody, true);
    //		javaMailSender.send(message);
    //	}

}
