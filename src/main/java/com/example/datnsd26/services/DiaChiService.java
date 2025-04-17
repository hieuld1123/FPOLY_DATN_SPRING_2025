package com.example.datnsd26.services;

import com.example.datnsd26.models.DiaChi;
import com.example.datnsd26.models.KhachHang;
import com.example.datnsd26.repository.DiaChiRepository;
import com.example.datnsd26.repository.KhachHangRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DiaChiService {
    @Autowired
    DiaChiRepository diaChiRepository;

    @Autowired
    KhachHangRepository khachHangRepository;

    public List<DiaChi> findByKhachHangId(Integer id) {
        return diaChiRepository.findByKhachHangId(id);
    }
    @Transactional
    public void xoaDiaChiTheoId(Integer id) {
        DiaChi diaChi = diaChiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ!"));

        KhachHang khachHang = diaChi.getKhachHang();

        khachHang.getDiaChi().remove(diaChi);
        khachHangRepository.save(khachHang);
        diaChiRepository.deleteById(id);
        System.out.println("Xóa thành công địa chỉ ID: " + id);
    }


    public Integer getKhachHangIdByDiaChiId(Integer diaChiId) {
        DiaChi diaChi = diaChiRepository.findById(diaChiId).orElse(null);
        return (diaChi != null && diaChi.getKhachHang() != null) ? diaChi.getKhachHang().getId() : null;
    }
    public List<DiaChi> findByKhachHang(KhachHang khachHang) {
        return diaChiRepository.findByKhachHang(khachHang);
    }
}
