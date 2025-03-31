package com.example.datnsd26.services;


import com.example.datnsd26.Dto.DiaChiDTO;
import com.example.datnsd26.Dto.KhachHangDto;
import com.example.datnsd26.Dto.NhanVienTKDto;
import com.example.datnsd26.models.DiaChi;
import com.example.datnsd26.models.KhachHang;
import com.example.datnsd26.models.NhanVien;
import com.example.datnsd26.models.TaiKhoan;
import com.example.datnsd26.repository.DiaChiRepository;
import com.example.datnsd26.repository.KhachHangRepository;
import com.example.datnsd26.repository.TaiKhoanRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TaiKhoanService {
    @Autowired
    TaiKhoanRepository taiKhoanRepository;

    @Autowired
    KhachHangRepository khachHangRepository;

    @Autowired
    EmailService emailService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    DiaChiRepository diaChiRepository;
    @Autowired
    JavaMailSender mailSender;

    public List<TaiKhoan> getAll() {
        return taiKhoanRepository.findAll();
    }

    public TaiKhoan findByEmail(String email) {
        return taiKhoanRepository.findByEmail(email).orElse(null);
    }

    public TaiKhoan save(TaiKhoan taiKhoan) {
        return taiKhoanRepository.save(taiKhoan);
    }

    private String taoChuoiNgauNhien(int doDaiChuoi, String kiTu) {
        Random random = new Random();
        StringBuilder chuoiNgauNhien = new StringBuilder(doDaiChuoi);
        for (int i = 0; i < doDaiChuoi; i++) {
            chuoiNgauNhien.append(kiTu.charAt(random.nextInt(kiTu.length())));
        }
        return chuoiNgauNhien.toString();
    }

    public void dangKy(KhachHangDto khachHangDto) throws MessagingException {
        TaiKhoan taiKhoan = new TaiKhoan();
        String matKhau = khachHangDto.getMatKhau();
        taiKhoan.setMatKhau(passwordEncoder.encode(matKhau));
        taiKhoan.setSdt(khachHangDto.getSdt());
        taiKhoan.setEmail(khachHangDto.getEmail());
        taiKhoan.setTrangThai(true);
        taiKhoan.setVaiTro(TaiKhoan.Role.CUSTOMER);
        taiKhoanRepository.save(taiKhoan);

        // Tạo khách hàng
        KhachHang khachHang = new KhachHang();
        khachHang.setMaKhachHang("KH" + taoChuoiNgauNhien(7, "0123456789"));
        khachHang.setTenKhachHang(khachHangDto.getTenKhachHang());
        khachHang.setGioiTinh(khachHangDto.getGioiTinh());
        khachHang.setNgaySinh(khachHangDto.getNgaySinh());
        khachHang.setHinhAnh(khachHangDto.getHinhAnh());
        khachHang.setNgayTao(new Timestamp(System.currentTimeMillis()));
        khachHang.setNgayCapNhat(new Timestamp(System.currentTimeMillis()));
        khachHang.setTrangThai(true);
        khachHang.setTaiKhoan(taiKhoan);
        khachHangRepository.save(khachHang);

        // Xử lý danh sách địa chỉ
        if (khachHangDto.getListDiaChi() != null && !khachHangDto.getListDiaChi().isEmpty()) {
            List<DiaChi> diaChiList = new ArrayList<>();
            for (DiaChiDTO diaChiDTO : khachHangDto.getListDiaChi()) {
                DiaChi diaChi = new DiaChi();
                diaChi.setTinh(diaChiDTO.getTinh());
                diaChi.setHuyen(diaChiDTO.getHuyen());
                diaChi.setXa(diaChiDTO.getXa());
                diaChi.setDiaChiCuThe(diaChiDTO.getDiaChiCuThe());
                diaChi.setTrangThai(true);
                diaChi.setKhachHang(khachHang);
                diaChiList.add(diaChi);
            }
            diaChiRepository.saveAll(diaChiList);
        }

        // Gửi email chứa thông tin tài khoản
        emailService.sendDangKyThanhCong(khachHangDto.getEmail(), khachHangDto.getTenKhachHang());
    }

    public boolean kiemTraEmailTonTai(String email) {
        return taiKhoanRepository.findByEmail(email).isPresent();
    }

    public void guiEmailDatLaiMatKhau(String email) {
        Optional<TaiKhoan> optionalTaiKhoan = taiKhoanRepository.findByEmail(email);
        if (optionalTaiKhoan.isPresent()) {
            TaiKhoan taiKhoan = optionalTaiKhoan.get();
            String token = UUID.randomUUID().toString(); // Tạo mã reset ngẫu nhiên

            // Lưu token vào database (giả sử bạn có cột `resetToken`)
            taiKhoan.setResetToken(token);
            taiKhoanRepository.save(taiKhoan);
            // Gửi email
            emailService.sendDoiMatKhauEmail(email, token);
        }
    }

    public Optional<TaiKhoan> timTaiKhoanBangToken(String token) {
        return taiKhoanRepository.findByResetToken(token);
    }

    public boolean datLaiMatKhau(String token, String matKhauMoi) {
        Optional<TaiKhoan> optionalTaiKhoan = taiKhoanRepository.findByResetToken(token);

        if (optionalTaiKhoan.isPresent()) {
            TaiKhoan taiKhoan = optionalTaiKhoan.get();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            taiKhoan.setMatKhau(passwordEncoder.encode(matKhauMoi)); // Mã hóa mật khẩu mới
            taiKhoan.setResetToken(null); // Xóa token sau khi đặt lại mật khẩu
            taiKhoanRepository.save(taiKhoan);
            return true;
        }
        return false;
    }
}