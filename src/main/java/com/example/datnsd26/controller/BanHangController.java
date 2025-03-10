package com.example.datnsd26.controller;

import com.example.datnsd26.services.BanHangService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ban-hang")
@Slf4j(topic = "BAN-HANG-CONTROLLER")
@RequiredArgsConstructor
public class BanHangController {
    private final BanHangService banHangService;

    @GetMapping
    public String banHang() {
        banHangService.getHoaDon();
        return "/admin/ban-hang/ban-hang";
    }
}
