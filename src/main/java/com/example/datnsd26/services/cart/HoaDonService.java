package com.example.datnsd26.services.cart;

import com.example.datnsd26.models.HoaDon;
import com.example.datnsd26.repository.HoaDonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HoaDonService {
    @Autowired
    private HoaDonRepository hoaDonRepository;

    public String generateMaHoaDon() {
        // Lấy mã hóa đơn lớn nhất từ database
        String lastMaHoaDon = hoaDonRepository.findLastMaHoaDon();

        if (lastMaHoaDon == null) {
            return "HD001"; // Nếu chưa có hóa đơn nào, bắt đầu từ HD001
        }

        // Tách số từ mã hóa đơn (VD: "HD005" -> 5)
        int number = Integer.parseInt(lastMaHoaDon.replace("HD", ""));

        // Tăng lên 1 đơn vị
        number++;

        // Định dạng lại mã hóa đơn (VD: 6 -> "HD006")
        return String.format("HD%03d", number);
    }

    public HoaDon saveHoaDon(HoaDon hoaDon) {
        // Gán mã hóa đơn trước khi lưu
        return hoaDonRepository.save(hoaDon);
    }
}
