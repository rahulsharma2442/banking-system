package com.example.bank.proj.commandfolder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final MailSender mailSender;

    // ✅ Constructor injection (preferred over field injection)
    @Autowired
    public EmailService(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendRegistrationEmail(String toEmail, String verificationLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("yourgmail@gmail.com"); // must match your configured username
            message.setTo(toEmail);
            message.setSubject("Verify Your Email");
            message.setText("Welcome!\n\nPlease verify your email by clicking this link:\n"
                    + verificationLink
                    + "\n\nThis link expires in 24 hours.\n\nThank you!");

            mailSender.send(message);
            System.out.println("✅ Verification email sent to: " + toEmail);

        } catch (Exception e) {
            // ✅ Log the exception for debugging
            System.err.println("❌ Failed to send email to: " + toEmail);
            e.printStackTrace();
        }
    }
}
