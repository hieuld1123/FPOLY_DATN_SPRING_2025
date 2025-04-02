package com.example.datnsd26.services;

import com.example.datnsd26.models.HoaDon;
import com.example.datnsd26.models.HoaDonChiTiet;
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

    public void sendOrderConfirmation(HoaDon hoaDon) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(hoaDon.getEmail());
        helper.setSubject("[NineShoes] Xác nhận đơn hàng của bạn");
        helper.setFrom("linhptkph30303@fpt.edu.vn");

        StringBuilder emailContent = new StringBuilder();
        emailContent.append("<h2>Cảm ơn bạn đã đặt hàng tại NineShoes!</h2>");
        emailContent.append("<p><strong>Mã hóa đơn:</strong> ").append(hoaDon.getMaHoaDon()).append("</p>");
        emailContent.append("<p><strong>Tên khách hàng:</strong> ").append(hoaDon.getTenNguoiNhan()).append("</p>");
        emailContent.append("<p><strong>Số điện thoại:</strong> ").append(hoaDon.getSdtNguoiNhan()).append("</p>");
        emailContent.append("<p><strong>Email:</strong> ").append(hoaDon.getEmail()).append("</p>");
        emailContent.append("<p><strong>Địa chỉ:</strong> ").append(hoaDon.getDiaChiNguoiNhan()).append("</p>");

        emailContent.append("<h3>Chi tiết đơn hàng:</h3>");
        emailContent.append("<table border='1' cellpadding='10' cellspacing='0' style='border-collapse: collapse; width: 100%;'>");
        emailContent.append("<tr><th>Sản phẩm</th><th>Đơn giá</th><th>Số lượng</th><th>Thành tiền</th></tr>");

        float tongTien = 0;
        for (HoaDonChiTiet chiTiet : hoaDon.getDanhSachSanPham()) {
            String tenSanPham = chiTiet.getSanPhamChiTiet().getSanPham().getTenSanPham();
            float giaBan = chiTiet.getGiaTienSauGiam();
            int soLuong = chiTiet.getSoLuong();
            float thanhTien = giaBan * soLuong;
            tongTien += thanhTien;

            emailContent.append("<tr>")
                    .append("<td>").append(tenSanPham).append("</td>")
                    .append("<td>").append(String.format("%,.0f", giaBan)).append(" VND</td>")
                    .append("<td>").append(soLuong).append("</td>")
                    .append("<td>").append(String.format("%,.0f", thanhTien)).append(" VND</td>")
                    .append("</tr>");
        }

        emailContent.append("</table>");
        emailContent.append("<p><strong>Phí vận chuyển:</strong> " + String.format("%,.0f", hoaDon.getPhiVanChuyen()) + " VND</p>");
        emailContent.append("<p><strong>Tổng tiền:</strong> " + String.format("%,.0f", (tongTien + hoaDon.getPhiVanChuyen())) + " VND</p>");

        emailContent.append("<h3>Cảm ơn bạn đã tin tưởng và mua sắm tại NineShoes!</h3>");
        emailContent.append("<p>Hotline hỗ trợ: <strong>0397 818 716</strong></p>");

        emailContent.append("<p>Để tra cứu đơn hàng của bạn, bạn có thể click <a href='http://nineshoes.com/tra-cuu?ma=")
                .append(hoaDon.getMaHoaDon())
                .append("-" + hoaDon.getSdtNguoiNhan())
                .append("'>TẠI ĐÂY</a> hoặc tra cứu trên website của chúng tôi với thông tin: <strong>")
                .append(hoaDon.getMaHoaDon()).append("-").append(hoaDon.getSdtNguoiNhan())
                .append("</strong></p>");

        helper.setText(emailContent.toString(), true);
        mailSender.send(message);
    }
}