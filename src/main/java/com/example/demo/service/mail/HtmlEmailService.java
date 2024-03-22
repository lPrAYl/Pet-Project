package com.example.demo.service.mail;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.time.LocalDate;

public class HtmlEmailService extends AbstractEmailService{

    private final JavaMailSender javaMailSender;

    public HtmlEmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendEmail() {
        String newVacancies = "Новые вакансии за " + LocalDate.now().minusDays(1);
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(sendTo);
            helper.setCc(sendCopy);
            helper.setFrom(from);
            helper.addAttachment("file.xlsx", attachment);
            helper.setSubject(newVacancies);
            helper.setText(newVacancies, true);
            javaMailSender.send(message);
            log.info("(sendHtmlMessage()) Отправлено " + logText);
        } catch (Exception e) {
            log.error("(sendHtmlMessage()) Не отправлено " + logText);
            log.error(e.getLocalizedMessage(), e);
        }
    }
}
