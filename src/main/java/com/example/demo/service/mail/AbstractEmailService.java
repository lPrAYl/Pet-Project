package com.example.demo.service.mail;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractEmailService implements Email{

    @Value("${spring.mail.username}")
    protected String from;
    @Value("${prop.mail.send_to}")
    protected String sendTo;
    @Value("${prop.mail.send_copy}")
    protected String sendCopy;

    protected String logText = "письмо на адрес: " + sendTo + ", копия: " + sendCopy;
    protected String noVacancies = "Нет вакансии за прошлые сутки";

    Logger log = (Logger) LoggerFactory.getLogger(AbstractEmailService.class);


}
