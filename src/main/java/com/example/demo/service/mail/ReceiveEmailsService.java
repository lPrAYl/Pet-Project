package com.example.demo.service.mail;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMultipart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

@Slf4j
@Service
public class ReceiveEmailsService {

    @Value("${prop.mail.imap}")
    private String host;
    @Value("${prop.mail.imap.port}")
    private int port;
    @Value("${spring.mail.username}")
    private String username;
    @Value("${spring.mail.password}")
    private String password;

    public void receiveEmails() {
        // Настройки подключения к почтовому серверу
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", port);
        properties.put("mail.imap.ssl.enable", "true");

        // Создаем сессию для подключения к почтовому серверу
        Session session = Session.getDefaultInstance(properties);
        try {
            // Подключение к почтовому серверу
            Store store = session.getStore("imaps");
            store.connect(host, port, username, password);
            // Получение папки входящих сообщений
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            // Получение всех сообщений из папки входящих сообщений
            Message[] messages = inbox.getMessages();

            for (Message message : messages) {
                // Обработка полученного письма
                log.info("Subject: " + message.getSubject());
                log.info("From: " + InternetAddress.toString(message.getFrom()));

                MimeMultipart multipart = (MimeMultipart) message.getContent();
                int numberOfParts = multipart.getCount();

                // Перебираем все части сообщения
                for (int partCount = 0; partCount < numberOfParts; partCount++) {
                    BodyPart bodyPart = multipart.getBodyPart(partCount);

                    // Проверяем, является ли часть сообщения вложением
                    if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                        // Проверяем, соответствует ли тип содержимого вложения типу XLSX
                        if (bodyPart.getContentType().contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                            // Получаем имя вложения
                            String attachmentName = bodyPart.getFileName();

                            // Сохраняем вложение на диск
                            InputStream inputStream = bodyPart.getInputStream();
                            OutputStream outputStream = new FileOutputStream(new File(attachmentName));

                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }

                            outputStream.close();
                            inputStream.close();
                            log.info("Найдено вложение: " + attachmentName);
                        }
                    }
                }

                // Помечаем сообщение для удаления
                message.setFlag(Flags.Flag.DELETED, true);
            }

            // Удаляем помеченные для удаления сообщения
            inbox.expunge();

            // Закрытие соединения
            inbox.close(false);
            store.close();
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

}
