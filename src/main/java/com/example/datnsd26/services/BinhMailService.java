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
        emailContent.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #eee; border-radius: 8px;'>");

        // Tiêu đề
        emailContent.append("<h2 style='color: #2c3e50; text-align: center;'>🎉 Cảm ơn bạn đã đặt hàng tại <span style='color:#e74c3c;'>Nine Shoes</span>!</h2>");

        // Thông tin đơn hàng
        emailContent.append("<p><strong>Mã hóa đơn:</strong> ").append(hoaDon.getMaHoaDon()).append("</p>");
        emailContent.append("<p><strong>Tên khách hàng:</strong> ").append(hoaDon.getTenNguoiNhan()).append("</p>");
        emailContent.append("<p><strong>Số điện thoại:</strong> ").append(hoaDon.getSdtNguoiNhan()).append("</p>");
        emailContent.append("<p><strong>Email:</strong> ").append(hoaDon.getEmail()).append("</p>");
        emailContent.append("<p><strong>Địa chỉ nhận hàng:</strong> ")
                .append(hoaDon.getDiaChiNguoiNhan())
                .append(", ").append(hoaDon.getXa())
                .append(", ").append(hoaDon.getQuan())
                .append(", ").append(hoaDon.getTinh())
                .append("</p>");
        // Chi tiết đơn hàng
        emailContent.append("<h3 style='margin-top: 30px; color:#34495e;'>🛍 Chi tiết đơn hàng</h3>");
        emailContent.append("<table style='width: 100%; border-collapse: collapse; margin-top: 10px;'>");
        emailContent.append("<thead style='background-color: #f8f8f8;'>");
        emailContent.append("<tr style='border-bottom: 1px solid #ddd;'>")
                .append("<th style='padding: 10px;'>Sản phẩm</th>")
                .append("<th style='padding: 10px;'>Đơn giá</th>")
                .append("<th style='padding: 10px;'>Số lượng</th>")
                .append("<th style='padding: 10px;'>Tổng</th>")
                .append("</tr>")
                .append("</thead><tbody>");

        float tongTien = 0;
        for (HoaDonChiTiet chiTiet : hoaDon.getDanhSachSanPham()) {
            String tenSanPham = chiTiet.getSanPhamChiTiet().getSanPham().getTenSanPham();
            float giaBan = chiTiet.getGiaTienSauGiam();
            int soLuong = chiTiet.getSoLuong();
            float thanhTien = giaBan * soLuong;
            tongTien += thanhTien;

            emailContent.append("<tr style='border-bottom: 1px solid #eee;'>")
                    .append("<td style='padding: 10px;'>").append(tenSanPham).append("</td>")
                    .append("<td style='padding: 10px;'>").append(String.format("%,.0f", giaBan)).append(" VND</td>")
                    .append("<td style='padding: 10px; text-align:center;'>").append(soLuong).append("</td>")
                    .append("<td style='padding: 10px;'>").append(String.format("%,.0f", thanhTien)).append(" VND</td>")
                    .append("</tr>");
        }
        emailContent.append("</tbody></table>");

        // Tổng cộng
        if (hoaDon.getVoucher() == null) {
            emailContent.append("<p style='margin-top: 20px;'><strong>🚚 Giảm giá:</strong> 0 VND</p>");
        } else {
            String giaTriGiam = String.format("%,.0f", hoaDon.getGiamGia());
            String maVoucher = hoaDon.getVoucher().getMaVoucher();
            String tenVoucher = hoaDon.getVoucher().getTenVoucher();
            emailContent.append("<p style='margin-top: 20px;'><strong>🚚 Giảm giá:</strong> ")
                    .append(giaTriGiam).append(" VND")
                    .append(" (").append(maVoucher).append(" - ").append(tenVoucher).append(")</p>");
        }

        emailContent.append("<p style='margin-top: 20px;'><strong>🚚 Phí vận chuyển:</strong> ")
                .append(String.format("%,.0f", hoaDon.getPhiVanChuyen())).append(" VND</p>");
        emailContent.append("<p style='font-size: 16px;'><strong>💰 Thành tiền:</strong> <span style='color:#e67e22;'>")
                .append(String.format("%,.0f", tongTien + hoaDon.getPhiVanChuyen() - hoaDon.getGiamGia()))
                .append(" VND</span></p>");

        // Cảm ơn và liên hệ
        emailContent.append("<hr style='margin: 30px 0;'/>");
        emailContent.append("<p style='font-style: italic;'>Một lần nữa, cảm ơn bạn đã tin tưởng và mua sắm tại <strong>NineShoes</strong>!</p>");
        emailContent.append("<p>📞 Hotline hỗ trợ: <strong>0397 818 716</strong></p>");

        // Link tra cứu
        emailContent.append("<p>🔍 Để tra cứu đơn hàng của bạn, vui lòng truy cập: ")
                .append("<a href='http://localhost:8080/order-tracking")
                .append("' style='color:#3498db;'>TẠI ĐÂY</a>");

        emailContent.append("</div>");

        helper.setText(emailContent.toString(), true);
        mailSender.send(message);
    }

}