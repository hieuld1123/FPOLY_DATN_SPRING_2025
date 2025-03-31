package com.example.datnsd26.controller;

import com.example.datnsd26.repository.HoaDonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
@RequiredArgsConstructor
@RequestMapping("/thong-ke")
public class ThongKeController {

    private final HoaDonRepository hoaDonRepository;

    @GetMapping
    public String thongKe(Model model) {
        Date today = new Date();

        Float doanhThu = hoaDonRepository.getDoanhThuHomNay(today);
        Long soDonHangMoi = hoaDonRepository.getSoDonHangMoi(today);
        Long soDonDaHuy = hoaDonRepository.getSoDonDaHuy(today);

        model.addAttribute("doanhThu", doanhThu);
        model.addAttribute("soDonHangMoi", soDonHangMoi);
        model.addAttribute("soDonDaHuy", soDonDaHuy);
        return "/admin/thong-ke/thong-ke";
    }

}
