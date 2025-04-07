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

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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


        List<String> allTrangThai = List.of(
                "Đặt hàng",
                "Chờ xác nhận",
                "Đã xác nhận",
                "Hoàn thành"

        );
        model.addAttribute("allTrangThai", allTrangThai);
        String currentStatus = hoaDon.getTrangThai();
        model.addAttribute("currentStatus", currentStatus);

        // Thời gian của các trạng thái đã qua
        Map<String, String> thoiGianFormattedMap = new LinkedHashMap<>();
        for (LichSuHoaDon lichSu : lichSuList) {
            String key = lichSu.getTrangThai().trim().toLowerCase();
            thoiGianFormattedMap.putIfAbsent(key, new SimpleDateFormat("dd/MM/yyyy HH:mm").format(lichSu.getThoiGian()));
        }

        model.addAttribute("thoiGianFormattedMap", thoiGianFormattedMap);
        model.addAttribute("hoaDon", hoaDon);
        model.addAttribute("allTrangThai", allTrangThai);
        model.addAttribute("lichSuList", lichSuList);
        model.addAttribute("chiTietList", chiTietList);

        return "shop/order-tracking";
    }


}
