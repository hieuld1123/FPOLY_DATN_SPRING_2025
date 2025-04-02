package com.example.datnsd26.controller;

import com.example.datnsd26.models.HoaDon;
import com.example.datnsd26.models.HoaDonChiTiet;
import com.example.datnsd26.models.LichSuHoaDon;
import com.example.datnsd26.repository.HoaDonChiTietRepository;
import com.example.datnsd26.repository.HoaDonRepository;
import com.example.datnsd26.repository.LichSuHoaDonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/order-tracking")
public class OrderTrackingController {
    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private LichSuHoaDonRepository lichSuHoaDonRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @GetMapping
    public String showTrackingPage() {
        return "/shop/order-tracking";
    }

    @PostMapping
    public String searchOrder(
            @RequestParam(value = "maHoaDon", required = false) String maHoaDon,
            @RequestParam(value = "sdtNguoiNhan", required = false) String sdtNguoiNhan,
            Model model) {

        if (maHoaDon == null || maHoaDon.trim().isEmpty() ||
                sdtNguoiNhan == null || sdtNguoiNhan.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Vui lòng nhập cả Mã hóa đơn và Số điện thoại.");
            return "shop/order-tracking";
        }

        Optional<HoaDon> optionalHoaDon = hoaDonRepository.findByMaHoaDonAndSdtNguoiNhan(maHoaDon, sdtNguoiNhan);
        if (optionalHoaDon.isEmpty()) {
            model.addAttribute("errorMessage", "Không tìm thấy đơn hàng với thông tin đã nhập.");
            return "shop/order-tracking";
        }

        HoaDon hoaDon = optionalHoaDon.get();
        List<HoaDonChiTiet> chiTietList = hoaDonChiTietRepository.findByHoaDon(hoaDon);
        List<LichSuHoaDon> lichSuList = lichSuHoaDonRepository.findByHoaDonOrderByThoiGianDesc(hoaDon);

        model.addAttribute("hoaDon", hoaDon);
        model.addAttribute("chiTietList", chiTietList);
        model.addAttribute("lichSuList", lichSuList);

        return "shop/order-tracking";
    }

}
