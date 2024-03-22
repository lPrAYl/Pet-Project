package com.example.demo.service.mail;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class SimpleEmailService extends AbstractEmailService {

    private final JavaMailSender javaMailSender;

    public SimpleEmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendEmail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(sendTo);
        message.setCc(sendCopy);
        message.setFrom(from);
        message.setSubject(noVacancies);
        message.setText(noVacancies);
        try {
            javaMailSender.send(message);
            log.info("(sendSimpleMessage()) Отправлено " + logText);
        } catch (Exception e) {
            log.error("(sendSimpleMessage()) Не отправлено " + logText);
            log.error(e.getLocalizedMessage(), e);
        }
    }
}
