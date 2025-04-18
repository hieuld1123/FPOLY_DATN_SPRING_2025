package com.example.datnsd26.services;

import com.example.datnsd26.Dto.NhanVienTKDto;
import com.example.datnsd26.models.NhanVien;
import com.example.datnsd26.models.TaiKhoan;
import com.example.datnsd26.repository.NhanVienRepository;
import com.example.datnsd26.repository.TaiKhoanRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.*;

@Service
public class NhanVienService {
    @Autowired
    NhanVienRepository nhanVienRepository;
    @Autowired
    TaiKhoanRepository taiKhoanRepository;
    @Autowired
    EmailService emailService;
    @Autowired
    PasswordEncoder passwordEncoder;

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();

    private static final String NUMBER = "0123456789";
    private static final String PASSWORD_ALLOW_BASE = CHAR_LOWER + CHAR_UPPER + NUMBER;
    private static final SecureRandom random = new SecureRandom();

    public Page<NhanVien> findAll(Pageable pageable) {
        return nhanVienRepository.findAll(pageable);
    }

    public List<NhanVien> getAll() {
        return nhanVienRepository.findAll();
    }

    public NhanVien getById(Integer id) {
        return nhanVienRepository.findById(id).orElse(null);
    }


    private String taoChuoiNgauNhien(int doDaiChuoi, String kiTu) {
        Random random = new Random();
        StringBuilder chuoiNgauNhien = new StringBuilder(doDaiChuoi);
        for (int i = 0; i < doDaiChuoi; i++) {
            chuoiNgauNhien.append(kiTu.charAt(random.nextInt(kiTu.length())));
        }
        return chuoiNgauNhien.toString();
    }


    public String generateRandomPassword(int length) {
        if (length < 4) throw new IllegalArgumentException("Length too short, minimum 4 characters required");
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(PASSWORD_ALLOW_BASE.charAt(random.nextInt(PASSWORD_ALLOW_BASE.length())));
        }
        return password.toString();
    }


    public NhanVien save(NhanVienTKDto nhanVien) throws MessagingException {
        TaiKhoan taiKhoan = new TaiKhoan();
        String matKhau = generateRandomPassword(10);
        String encoderMatKhau = passwordEncoder.encode(matKhau);
        taiKhoan.setMatKhau(encoderMatKhau);
        taiKhoan.setSdt(nhanVien.getSdt());
        taiKhoan.setEmail(nhanVien.getEmail());
        taiKhoan.setTrangThai(true);
        taiKhoan.setVaiTro(TaiKhoan.Role.EMPLOYEE);
        taiKhoanRepository.save(taiKhoan);

        NhanVien nv = new NhanVien();

        String chuoiNgauNhien = taoChuoiNgauNhien(7, "0123456789");
        String maNV = "NV" + chuoiNgauNhien;
        nv.setTenNhanVien(nhanVien.getTenNhanVien());
        nv.setMaNhanvien(maNV);
        nv.setHinhAnh(nhanVien.getHinhAnh());
        nv.setXa(nhanVien.getXa());
        nv.setHuyen(nhanVien.getHuyen());
        nv.setTinh(nhanVien.getTinh());
        nv.setDiaChiCuThe(nhanVien.getDiaChiCuThe());
        nv.setNgaySinh(nhanVien.getNgaySinh());
        nv.setGioiTinh(nhanVien.getGioiTinh());
        nv.setNgayTao(new Timestamp(new Date().getTime()));
        nv.setNgayCapNhat(new Timestamp(new Date().getTime()));
        nv.setTaiKhoan(taiKhoan);
        nv.setTrangThai(true);
        nv = nhanVienRepository.save(nv);
        // Gửi email chứa thông tin tài khoản
        emailService.sendNewEmployeeAccountEmail(nhanVien.getEmail(), matKhau);
        return nv;
    }

    public NhanVien update(NhanVienTKDto nhanVien, Integer id) {
        NhanVien existingNhanVien = nhanVienRepository.getById(nhanVien.getId());
        existingNhanVien.setTenNhanVien(nhanVien.getTenNhanVien());
        existingNhanVien.setHinhAnh(nhanVien.getHinhAnh());
        existingNhanVien.setXa(nhanVien.getXa());
        existingNhanVien.setHuyen(nhanVien.getHuyen());
        existingNhanVien.setTinh(nhanVien.getTinh());
        existingNhanVien.setDiaChiCuThe(nhanVien.getDiaChiCuThe());
        existingNhanVien.setNgaySinh(nhanVien.getNgaySinh());
        existingNhanVien.setGioiTinh(nhanVien.getGioiTinh());
        existingNhanVien.setTrangThai(nhanVien.getTrangThai());
        existingNhanVien.setNgayCapNhat(new Timestamp(new Date().getTime()));


        TaiKhoan existingTaiKhoan = existingNhanVien.getTaiKhoan();
        existingTaiKhoan.setSdt(nhanVien.getSdt());
        existingTaiKhoan.setEmail(nhanVien.getEmail());
//        existingTaiKhoan.setVaiTro(nhanVien.getVaiTro());
        if (existingNhanVien.getTrangThai() == false) {
            existingTaiKhoan.setTrangThai(false);
        } else {
            existingTaiKhoan.setTrangThai(true);
        }
        return nhanVienRepository.save(existingNhanVien);

    }

    public Page<NhanVien> findByTenSdtMaTT(String tenSdtMa, Boolean trangthai, String role, Pageable pageable) {
        // Chuyển đổi từ String sang Enum
        TaiKhoan.Role vaiTro = null;
        if (role != null && !role.isEmpty()) {
            try {
                vaiTro = TaiKhoan.Role.valueOf(role.toUpperCase()); // Chuyển String thành Enum
            } catch (IllegalArgumentException e) {
                vaiTro = null; // Nếu không thể chuyển đổi được thì để null
            }
        }

        return nhanVienRepository.searchByTenOrSdtOrTrangThai(tenSdtMa, trangthai, vaiTro, pageable);
    }

    public NhanVien findByEmail(String email) {
        return nhanVienRepository.findByEmail(email).orElse(null);
    }
}

