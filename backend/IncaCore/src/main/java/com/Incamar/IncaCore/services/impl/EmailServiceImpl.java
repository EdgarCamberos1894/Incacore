package com.Incamar.IncaCore.services.impl;

import com.Incamar.IncaCore.exceptions.EmailSendingException;
import com.Incamar.IncaCore.services.EmailService;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final Resend resend;
    private final SpringTemplateEngine templateEngine;

    @Value("${resend.email}")
    private String senderEmail;

    @Override
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);
            String htmlBody = templateEngine.process(templateName, context);

            CreateEmailOptions email = CreateEmailOptions.builder()
                    .from(senderEmail)
                    .to(to)
                    .subject(subject)
                    .html(htmlBody)
                    .build();

            resend.emails().send(email);
        } catch (ResendException e) {
            throw new EmailSendingException("Failed to send email", e);
        }
    }

    @Override
    public void sendStockAlertEmail(String to, String recipientName, String productName, Long currentStock, String warehouseName, Long minimumStock) {
        Map<String, Object> variables = Map.of(
                "recipientName", recipientName,
                "productName", productName,
                "warehouseName", warehouseName,
                "currentStock", currentStock,
                "minimumStock", minimumStock
        );

        sendHtmlEmail(to, "Alerta de Stock Minimo", "stock-alert-email", variables);
    }
}
