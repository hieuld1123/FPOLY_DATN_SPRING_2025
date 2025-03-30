package com.example.datnsd26.services;

import com.example.datnsd26.models.DiaChi;
import com.example.datnsd26.models.KhachHang;
import com.example.datnsd26.repository.DiaChiRepository;
import com.example.datnsd26.repository.KhachHangRepository;
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
    public void xoaDiaChiTheoId(Integer id) {
        DiaChi diaChi = diaChiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ!"));

        KhachHang khachHang = diaChi.getKhachHang();
        // Nếu địa chỉ này là mặc định, chọn một địa chỉ khác làm mặc định
        if (diaChi.getTrangThai()) {
            Optional<DiaChi> diaChiMoi = khachHang.getDiaChi().stream()
                    .filter(dc -> !dc.getId().equals(id)) // Lọc ra địa chỉ khác
                    .findFirst(); // Lấy địa chỉ đầu tiên

            diaChiMoi.ifPresent(dc -> dc.setTrangThai(true));
        }
        khachHang.getDiaChi().remove(diaChi);

        khachHangRepository.save(khachHang);

        diaChiRepository.deleteById(id);
    }

}
