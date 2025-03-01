package com.example.datnsd26.services;

import com.example.datnsd26.Dto.DiaChiDTO;
import com.example.datnsd26.Dto.KhachHangDto;
import com.example.datnsd26.models.DiaChi;
import com.example.datnsd26.models.KhachHang;
import com.example.datnsd26.models.NhanVien;
import com.example.datnsd26.models.TaiKhoan;
import com.example.datnsd26.repository.DiaChiRepository;
import com.example.datnsd26.repository.KhachHangRepository;
import com.example.datnsd26.repository.TaiKhoanRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class KhachHangService {
    @Autowired
    private KhachHangRepository khachHangRepository;
    @Autowired
    TaiKhoanRepository taiKhoanRepository;
    @Autowired
    DiaChiRepository diaChiRepository;
    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();

    private static final String NUMBER = "0123456789";
    private static final String PASSWORD_ALLOW_BASE = CHAR_LOWER + CHAR_UPPER + NUMBER;
    private static final SecureRandom random = new SecureRandom();

    public List<KhachHang> getAll() {
        return khachHangRepository.findAll();
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

    @Transactional
    public void save(KhachHangDto khachHangDto) {
        // Tạo và lưu tài khoản
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setSdt(khachHangDto.getSdt());
        taiKhoan.setMatKhau(generateRandomPassword(10));
        taiKhoan.setEmail(khachHangDto.getEmail());
        taiKhoan.setTrangThai(true);
        taiKhoan.setVaiTro(TaiKhoan.Role.CUSTOMER);
        taiKhoan = taiKhoanRepository.save(taiKhoan); // Lưu tài khoản vào DB

        // Tạo khách hàng
        KhachHang khachHang = new KhachHang();
        khachHang.setMaKhachHang("KH" + taoChuoiNgauNhien(7, "0123456789"));
        khachHang.setTenKhachHang(khachHangDto.getTenKhachHang());
        khachHang.setGioiTinh(khachHangDto.getGioiTinh());
        khachHang.setNgaySinh(khachHangDto.getNgaySinh());
        khachHang.setNgayTao(new Timestamp(System.currentTimeMillis()));
        khachHang.setNgayCapNhat(new Timestamp(System.currentTimeMillis()));
        khachHang.setTrangThai(true);
        khachHang.setTaiKhoan(taiKhoan);

        khachHang = khachHangRepository.save(khachHang); // Lưu khách hàng trước

        // Lưu danh sách địa chỉ
        if (khachHangDto.getListDiaChi() != null && !khachHangDto.getListDiaChi().isEmpty()) {
            boolean macDinh = false;
            for (DiaChiDTO diaChiDTO : khachHangDto.getListDiaChi()) {
                DiaChi diaChi = new DiaChi();
                diaChi.setTinh(diaChiDTO.getTinh());
                diaChi.setHuyen(diaChiDTO.getHuyen());
                diaChi.setXa(diaChiDTO.getXa());
                diaChi.setDiaChiCuThe(diaChiDTO.getDiaChiCuThe());
                diaChi.setNgayTao(new Timestamp(System.currentTimeMillis()));
                diaChi.setNgayCapNhat(new Timestamp(System.currentTimeMillis()));
                // Nếu chưa có địa chỉ mặc định, đặt cái đầu tiên làm mặc định
                if (!macDinh) {
                    diaChi.setTrangThai(true); // Địa chỉ mặc định
                    macDinh = true; // Đánh dấu đã có địa chỉ mặc định
                } else {
                    diaChi.setTrangThai(false); // Các địa chỉ còn lại không mặc định
                }
                diaChi.setKhachHang(khachHang);
                diaChiRepository.save(diaChi);
            }
        } else {
            System.out.println("listDiaChi của khachHangDto bị null hoặc rỗng!");
        }
        khachHangRepository.save(khachHang); // Lưu lại khách hàng với danh sách địa chỉ
    }


    public KhachHang getById(Integer id) {

        return khachHangRepository.findById(id).orElse(null);
    }

    public KhachHang update(KhachHangDto khachHangDto,Integer id) {
        KhachHang khachHang = khachHangRepository.getById(id);
        khachHang.setHinhAnh(khachHangDto.getHinhAnh());
        khachHang.setMaKhachHang(khachHangDto.getMaKhachHang());
        khachHang.setTenKhachHang(khachHangDto.getTenKhachHang());
        khachHang.setTrangThai(khachHangDto.getTrangThai());
        khachHang.setGioiTinh(khachHangDto.getGioiTinh());
        khachHang.setNgaySinh(khachHangDto.getNgaySinh());
        khachHang.setNgayCapNhat(new Timestamp(new Date().getTime()));

        TaiKhoan existingTaiKhoan = khachHang.getTaiKhoan();
        existingTaiKhoan.setSdt(khachHangDto.getSdt());;
        existingTaiKhoan.setEmail(khachHangDto.getEmail());
        existingTaiKhoan.setTrangThai(khachHangDto.getTrangThai());
        existingTaiKhoan.setVaiTro(khachHangDto.getVaiTro());
        taiKhoanRepository.save(existingTaiKhoan);

        List<DiaChi> danhSachDiaChi = diaChiRepository.findByKhachHangId(id);
        List<DiaChiDTO> listDiaChiDTO = khachHangDto.getListDiaChi();

        for (int i = 0; i < danhSachDiaChi.size(); i++) {
            DiaChi diaChi = danhSachDiaChi.get(i);
            DiaChiDTO diaChiDTO = listDiaChiDTO.get(i);

            diaChi.setId(diaChiDTO.getId());
            diaChi.setTinh(diaChiDTO.getTinh());
            diaChi.setHuyen(diaChiDTO.getHuyen());
            diaChi.setXa(diaChiDTO.getXa());
            diaChi.setDiaChiCuThe(diaChiDTO.getDiaChiCuThe());
            diaChi.setTrangThai(diaChiDTO.getTrangThai());

            diaChiRepository.save(diaChi);
        }

        return khachHangRepository.save(khachHang);
    }

}
