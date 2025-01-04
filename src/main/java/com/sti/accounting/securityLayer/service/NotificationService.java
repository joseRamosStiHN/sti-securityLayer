package com.sti.accounting.securityLayer.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sti.accounting.securityLayer.utils.TypeSMS;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    @Value("${app.account.sid}")
    private String accountSid;

    @Value("${app.auth.token}")
    private String authToken;

    @Value("${app.send.grid.key}")
    private String sendGridKey;


    @PostConstruct
    private void init() {
        initTwilio();
    }

    private void initTwilio() {
        if(isNullOrEmpty(accountSid) ) {
            this.accountSid = System.getenv("TWILIO_ACCOUNT_SID");
        }
        if( isNullOrEmpty(authToken)) {
            this.authToken = System.getenv("TWILIO_AUTH_TOKEN");
        }

        if (!isNullOrEmpty(accountSid) && !isNullOrEmpty(authToken)) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio initialized successfully");
        } else {
            log.warn("Twilio credentials not found - SMS functionality will be disabled");
        }
    }


    private boolean isNullOrEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }


    public void sendEmail(String from, String to, String subject, String message) {
        log.info("Sending Email to: {} with message: {}", to, message);
        try{
            Email fromEmail = new Email(from);
            Email toEmail = new Email(to);

            Content content = new Content("text/html", message);

            Mail mail = new Mail(fromEmail, subject, toEmail, content);
            SendGrid sg = new SendGrid(sendGridKey);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setBody(mail.build());
            Response response = sg.api(request);

            log.info("email sent to {}, status {}", to, response.getStatusCode());

        }catch (Exception e){
            log.error("Error sending email to {} ", to, e);
        }
    }
    // this need the whatsApp business account
    // for both phone number its required the code area for example +504 for Honduras, +505 for Nicaragua and so on.
    public void sendSms(String from, String to, String message, TypeSMS type) {
        log.info("Sending WhatsApp message");
        try{
            Message whatsappMessage = Message.creator(
                    new PhoneNumber(String.format("%s%s", type.getValue(), from)),
                    new PhoneNumber(String.format("%s%s", type.getValue(), to)),
                    message).create();
            log.info("message to {} status {} ", to, whatsappMessage.getStatus());
        }catch (Exception e){
            log.error("Error sending message to {} ", to, e);
        }
    }
}
