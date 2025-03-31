package com.example.datnsd26.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
@EnableAsync
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendNewEmployeeAccountEmail(String toEmail, String tempPassword) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("linhptkph30303@fpt.edu.vn");
        helper.setTo(toEmail);
        helper.setSubject("Tài khoản nhân viên mới");
        String resetPasswordLink = "http://localhost:8080/doi-mat-khau?email=" + toEmail;
        String emailContent = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; 
                                border: 1px solid #ddd; border-radius: 8px; background-color: #f9f9f9;">
                        <h2 style="color: #333; text-align: center;">🎉 Chào mừng bạn đến với Nine Shoes Store! 🎉</h2>
                        <p style="font-size: 16px; color: #555;">Xin chào <strong>%s</strong>,</p>
                        <p style="font-size: 16px; color: #555;">Bạn đã được cấp tài khoản nhân viên tại <strong>Nine Shoes Store</strong>.</p>
                        
                        <div style="background: #fff; padding: 15px; border-radius: 5px; border: 1px solid #ddd;">
                            <p style="font-size: 16px; color: #333;"><strong>📧 Email đăng nhập:</strong> %s</p>
                            <p style="font-size: 16px; color: #333;"><strong>🔑 Mật khẩu tạm thời:</strong> %s</p>
                        </div>

                        <p style="font-size: 16px; color: #555;">Vui lòng đổi mật khẩu ngay để bảo mật tài khoản của bạn.</p>

                        <div style="text-align: center; margin-top: 20px;">
                            <a href="%s" style="background-color: #007bff; color: white; padding: 12px 20px; 
                                               text-decoration: none; font-size: 16px; border-radius: 5px; display: inline-block;">
                                🔒 Đổi mật khẩu ngay
                            </a>
                        </div>

                        <p style="font-size: 14px; color: #777; margin-top: 20px;">Nếu bạn không yêu cầu tài khoản này, vui lòng liên hệ ngay với quản trị viên.</p>

                        <hr style="border: none; border-top: 1px solid #ddd; margin: 20px 0;">
                        <p style="text-align: center; font-size: 14px; color: #777;">🚀 Nine Shoes Store - Hỗ trợ khách hàng 24/7</p>
                    </div>
                """.formatted(toEmail, toEmail, tempPassword, resetPasswordLink);


        helper.setText(emailContent, true);
        mailSender.send(message);
    }
    @Async
    public void sendNewCustomerAccountEmail(String toEmail, String tempPassword) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("linhptkph30303@fpt.edu.vn");
        helper.setTo(toEmail);
        helper.setSubject("Tài khoản khách hàng mới");
        String resetPasswordLink = "http://localhost:8080/doi-mat-khau?email=" + toEmail;
        String emailContent = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; 
                                border: 1px solid #ddd; border-radius: 8px; background-color: #f9f9f9;">
                        <h2 style="color: #333; text-align: center;">🎉 Chào mừng bạn đến với Nine Shoes Store! 🎉</h2>
                        <p style="font-size: 16px; color: #555;">Xin chào <strong>%s</strong>,</p>
                        <p style="font-size: 16px; color: #555;">Bạn đã được cấp tài khoản thành viên tại <strong>Nine Shoes Store</strong>.</p>
                        
                        <div style="background: #fff; padding: 15px; border-radius: 5px; border: 1px solid #ddd;">
                            <p style="font-size: 16px; color: #333;"><strong>📧 Email đăng nhập:</strong> %s</p>
                            <p style="font-size: 16px; color: #333;"><strong>🔑 Mật khẩu tạm thời:</strong> %s</p>
                        </div>

                        <p style="font-size: 16px; color: #555;">Vui lòng đổi mật khẩu ngay để bảo mật tài khoản của bạn.</p>

                        <div style="text-align: center; margin-top: 20px;">
                            <a href="%s" style="background-color: #007bff; color: white; padding: 12px 20px; 
                                               text-decoration: none; font-size: 16px; border-radius: 5px; display: inline-block;">
                                🔒 Đổi mật khẩu ngay
                            </a>
                        </div>

                        <p style="font-size: 14px; color: #777; margin-top: 20px;">Nếu bạn không yêu cầu tài khoản này, vui lòng liên hệ ngay với quản trị viên.</p>

                        <hr style="border: none; border-top: 1px solid #ddd; margin: 20px 0;">
                        <p style="text-align: center; font-size: 14px; color: #777;">🚀 Nine Shoes Store - Hỗ trợ khách hàng 24/7</p>
                    </div>
                """.formatted(toEmail, toEmail, tempPassword, resetPasswordLink);


        helper.setText(emailContent, true);
        mailSender.send(message);
    }

    @Async
    public void sendDoiMatKhauEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("linhptkph30303@fpt.edu.vn");
            helper.setTo(toEmail);
            helper.setSubject("🔒 Yêu cầu đặt lại mật khẩu - Nine Shoes Store");

            String resetPasswordLink = "http://localhost:8080/dat-lai-mat-khau?token=" + token;
            String emailContent = """
                        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; 
                                    border: 1px solid #ddd; border-radius: 8px; background-color: #f9f9f9;">
                            <h2 style="color: #333; text-align: center;">🔐 Đặt lại mật khẩu của bạn 🔐</h2>
                            <p style="font-size: 16px; color: #555;">Xin chào <strong>%s</strong>,</p>
                            <p style="font-size: 16px; color: #555;">Chúng tôi nhận được yêu cầu đặt lại mật khẩu của bạn tại <strong>Nine Shoes Store</strong>.</p>
                            
                            <p style="font-size: 16px; color: #555;">Vui lòng nhấn vào nút bên dưới để đặt lại mật khẩu:</p>

                            <div style="text-align: center; margin-top: 20px;">
                                <a href="%s" style="background-color: #007bff; color: white; padding: 12px 20px; 
                                                   text-decoration: none; font-size: 16px; border-radius: 5px; display: inline-block;">
                                    🔑 Đặt lại mật khẩu
                                </a>
                            </div>

                            <p style="font-size: 14px; color: #777; margin-top: 20px;">Nếu bạn không yêu cầu điều này, vui lòng bỏ qua email này hoặc liên hệ với bộ phận hỗ trợ.</p>

                            <hr style="border: none; border-top: 1px solid #ddd; margin: 20px 0;">
                            <p style="text-align: center; font-size: 14px; color: #777;">🚀 Nine Shoes Store - Hỗ trợ khách hàng 24/7</p>
                        </div>
                    """.formatted(toEmail, resetPasswordLink);

            helper.setText(emailContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Gửi email thất bại, vui lòng thử lại!");
        }
    }

    @Async
    public void sendDangKyThanhCong(String toEmail, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("linhptkph30303@fpt.edu.vn");
            helper.setTo(toEmail);
            helper.setSubject("🎉 Chúc mừng! Bạn đã đăng ký thành công tài khoản - Nine Shoes Store");

            String emailContent = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; 
                                border: 1px solid #ddd; border-radius: 8px; background-color: #f9f9f9;">
                        <h2 style="color: #333; text-align: center;">🎉 Chúc mừng, %s! 🎉</h2>
                        <p style="font-size: 16px; color: #555;">Xin chào <strong>%s</strong>,</p>
                        <p style="font-size: 16px; color: #555;">Bạn đã đăng ký thành công tài khoản tại <strong>Nine Shoes Store</strong>.</p>
                        
                        <p style="font-size: 16px; color: #555;">Tận hưởng trải nghiệm mua sắm tuyệt vời và khám phá những sản phẩm mới nhất ngay hôm nay!</p>

                        <div style="text-align: center; margin-top: 20px;">
                            <a href="http://localhost:8080/dang-nhap" style="background-color: #28a745; color: white; padding: 12px 20px; 
                                               text-decoration: none; font-size: 16px; border-radius: 5px; display: inline-block;">
                                🚀 Đăng nhập ngay
                            </a>
                        </div>

                        <p style="font-size: 14px; color: #777; margin-top: 20px;">Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với bộ phận hỗ trợ của chúng tôi.</p>

                        <hr style="border: none; border-top: 1px solid #ddd; margin: 20px 0;">
                        <p style="text-align: center; font-size: 14px; color: #777;">🚀 Nine Shoes Store - Hỗ trợ khách hàng 24/7</p>
                    </div>
                """.formatted(fullName, fullName);

            helper.setText(emailContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Gửi email thất bại, vui lòng thử lại!");
        }
    }

}
