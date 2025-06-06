package com.sti.accounting.security_layer.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sti.accounting.security_layer.utils.TypeSMS;
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
        if (isNullOrEmpty(accountSid)) {
            this.accountSid = System.getenv("TWILIO_ACCOUNT_SID");
        }
        if (isNullOrEmpty(authToken)) {
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
        try {
            Email fromEmail = new Email(from);
            Email toEmail = new Email(to);
            Content content = new Content("text/html", message);

            Mail mail = new Mail(fromEmail, subject, toEmail, content);
            SendGrid sg = new SendGrid(sendGridKey);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            log.info("email sent to {}, status {}", to, response.getStatusCode());
            log.info("Response body: {}", response.getBody());

            if (response.getStatusCode() >= 400) {
                throw new RuntimeException("Failed to send email. Status: " + response.getStatusCode()
                        + ", Body: " + response.getBody());
            }
        } catch (Exception e) {
            log.error("Error sending email to {}", to, e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    // this need the whatsApp business account
    // for both phone number its required the code area for example +504 for Honduras, +505 for Nicaragua and so on.
    public void sendSms(String from, String to, String message, TypeSMS type) {
        log.info("Sending WhatsApp message");
        try {
            Message whatsappMessage = Message.creator(
                    new PhoneNumber(String.format("%s%s", type.getValue(), from)),
                    new PhoneNumber(String.format("%s%s", type.getValue(), to)),
                    message).create();
            log.info("message to {} status {} ", to, whatsappMessage.getStatus());
        } catch (Exception e) {
            log.error("Error sending message to {} ", to, e);
        }
    }

    /**
     * Construye el contenido HTML para el correo de recuperación de contraseña
     *
     * @param userName    Nombre del usuario (opcional)
     * @param newPassword Nueva contraseña generada
     * @return String con el contenido HTML del correo
     */
    public String buildRecoveryEmail(String userName, String newPassword) {
        return "<!DOCTYPE html>" +
                "<html lang='es'>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <title>Recuperación de Contraseña</title>" +
                "    <style>" +
                "        body { font-family: 'Arial', sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f5f5f5; }" +
                "        .container { max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 0 20px rgba(0,0,0,0.1); }" +
                "        .header { background-color: #2563eb; padding: 20px; text-align: center; }" +
                "        .header img { max-width: 150px; }" +
                "        .content {font-size: 14px; padding: 30px; }" +
                "        h1 { text-align: center; color: #2563eb; margin-top: 0; }" +
                "        .password-box { background: #f0f7ff; border: 1px dashed #2563eb; border-radius: 6px; padding: 15px; text-align: center; margin: 20px 0; font-size: 24px; font-weight: bold; color: #1e40af; }" +
                "        .button { display: inline-block; background-color: #2563eb; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; font-weight: bold; margin: 10px 0; }" +
                "        .footer { background-color: #f1f5f9; padding: 20px; text-align: center; font-size: 14px; color: #64748b; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <h1>Recuperación de Contraseña</h1>" +
                "            <p>Hola " + (userName != null ? userName : "") + ",</p>" +
                "            <p>Hemos recibido una solicitud para restablecer tu contraseña. A continuación encontrarás tu nueva contraseña temporal:</p>" +
                "            <div class='password-box'>" + newPassword + "</div>" +
                "            <p>Por seguridad, te recomendamos cambiar esta contraseña después de iniciar sesión.</p>" +
                "            <p>Si no solicitaste este cambio, por favor contacta a nuestro equipo de soporte inmediatamente.</p>" +
                "            <p>Atentamente,</p>" +
                "            <p>El equipo de STI Globals</p>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>© 2025 STI Globals. Todos los derechos reservados.</p>" +
                "            <p>Este es un correo automático, por favor no responder directamente.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

}
