package com.example.datnsd26.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class BinhMailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOrderConfirmation(String toEmail, String orderDetails) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("Xác nhận đơn hàng của bạn");
        helper.setText("<h2>Đơn hàng của bạn đã được đặt thành công!</h2><p>" + orderDetails + "</p>", true);
        helper.setFrom("hoangquocbinh0411@gmail.com");

        mailSender.send(message);
    }
}