package com.example.datnsd26.services;

import com.example.datnsd26.Dto.NhanVienTKDto;
import com.example.datnsd26.models.NhanVien;
import com.example.datnsd26.models.TaiKhoan;
import com.example.datnsd26.repository.NhanVienRepository;
import com.example.datnsd26.repository.TaiKhoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();

    private static final String NUMBER = "0123456789";
    private static final String PASSWORD_ALLOW_BASE = CHAR_LOWER + CHAR_UPPER + NUMBER;
    private static final SecureRandom random = new SecureRandom();

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


    public NhanVien save(NhanVienTKDto nhanVien) {
        TaiKhoan taiKhoan = new TaiKhoan();
        String matKhau = generateRandomPassword(10);
        taiKhoan.setSdt(nhanVien.getSdt());
        taiKhoan.setMatKhau(matKhau);
        taiKhoan.setEmail(nhanVien.getEmail());
        taiKhoan.setTrangThai(true);
        taiKhoan.setVaiTro(nhanVien.getVaiTro());

        NhanVien nv = new NhanVien();

        String chuoiNgauNhien = taoChuoiNgauNhien(7, "0123456789");
        String maNV = "NV" + chuoiNgauNhien;
        nv.setTenNhanVien(nhanVien.getTenNhanVien());
        nv.setMaNhanvien(maNV);
        nv.setCccd(nhanVien.getCccd());
        nv.setHinhAnh(nhanVien.getHinhAnh());
        nv.setQuan(nhanVien.getQuan());
        nv.setPhuong(nhanVien.getPhuong());
        nv.setTinh(nhanVien.getTinh());
        nv.setDiaChiCuThe(nhanVien.getDiaChiCuThe());
        nv.setNgaySinh(nhanVien.getNgaySinh());
        nv.setGioiTinh(nhanVien.getGioiTinh());
        nv.setNgayTao(new Timestamp(new Date().getTime()));
        nv.setNgayCapNhat(new Timestamp(new Date().getTime()));
        nv.setTaiKhoan(taiKhoan);
        nv.setTrangThai(true);

        return nhanVienRepository.save(nv);
    }

    public NhanVien update(NhanVienTKDto nhanVien, Integer id) {
        NhanVien existingNhanVien = nhanVienRepository.getById(nhanVien.getId());
        existingNhanVien.setTenNhanVien(nhanVien.getTenNhanVien());
        existingNhanVien.setCccd(nhanVien.getCccd());
        existingNhanVien.setHinhAnh(nhanVien.getHinhAnh());
        existingNhanVien.setQuan(nhanVien.getQuan());
        existingNhanVien.setPhuong(nhanVien.getPhuong());
        existingNhanVien.setTinh(nhanVien.getTinh());
        existingNhanVien.setDiaChiCuThe(nhanVien.getDiaChiCuThe());
        existingNhanVien.setNgaySinh(nhanVien.getNgaySinh());
        existingNhanVien.setGioiTinh(nhanVien.getGioiTinh());
        existingNhanVien.setTrangThai(nhanVien.getTrangThai());
        existingNhanVien.setNgayCapNhat(new Timestamp(new Date().getTime()));


        TaiKhoan existingTaiKhoan = existingNhanVien.getTaiKhoan();
        existingTaiKhoan.setSdt(nhanVien.getSdt());
        existingTaiKhoan.setEmail(nhanVien.getEmail());
        existingTaiKhoan.setTrangThai(nhanVien.getTrangThai());
        existingTaiKhoan.setVaiTro(nhanVien.getVaiTro());
        if (existingNhanVien.getTrangThai() == false) {
            existingTaiKhoan.setTrangThai(false);
        } else {
            existingTaiKhoan.setTrangThai(true);
        }
        return nhanVienRepository.save(existingNhanVien);

    }

    public List<NhanVien> findByTenSdtMaTT(String tenSdtMa, Boolean trangthai) {
        return nhanVienRepository.searchByTenOrSdtOrTrangThai(tenSdtMa, trangthai);
    }
//    public String delete(Integer id) {
//        if (nhanVienRepository.existsById(id)) {
//            nhanVienRepository.deactivateNhanVien(id);
//            return "Nhân viên đã được vô hiệu hóa!";
//        } else {
//            return "Không tìm thấy nhân viên!";
//        }
//    }

}

