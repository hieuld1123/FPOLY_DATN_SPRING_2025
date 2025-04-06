package com.example.datnsd26.controller;

import com.example.datnsd26.repository.HoaDonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/hoa-don")
@Slf4j(topic = "HOA-DON-CONTROLLER")
@RequiredArgsConstructor
public class HoaDonController {

    private final HoaDonRepository hoaDonRepository;

    @GetMapping("/tat-ca-hoa-don")
    public String hoaDonTatCaHoaDon(Model model) {
        model.addAttribute("hoaDonList", hoaDonRepository.findAll(Sort.by(Sort.Direction.DESC, "ngayTao")));
        return "/admin/hoa-don/hoa-don-all";
    }


    @GetMapping
    public String getInvoices() {
        return "/admin/hoa-don/overview-hoa-don";
    }

    @GetMapping("/{id}")
    public String getInvoice(@PathVariable String id) {
        return "/admin/hoa-don/detail-hoa-don";
    }
}
