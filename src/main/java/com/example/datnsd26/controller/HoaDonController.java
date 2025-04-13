package com.example.datnsd26.controller;

import com.example.datnsd26.services.HoaDonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/quan-ly/hoa-don")
@Slf4j(topic = "HOA-DON-CONTROLLER")
@RequiredArgsConstructor
public class HoaDonController {

    private final HoaDonService hoaDonService;

    @GetMapping
    public String getInvoices() {
        return "/admin/hoa-don/invoices";
    }

    @GetMapping("/{id}")
    public String getInvoice(@PathVariable String id) {
        return "/admin/hoa-don/invoice-detail";
    }

    @GetMapping("/edit/{code}")
    public String editInvoice(@PathVariable String code) {
        this.hoaDonService.createHistoryModify(code);
        return "/admin/hoa-don/invoice-modify";
    }
}
