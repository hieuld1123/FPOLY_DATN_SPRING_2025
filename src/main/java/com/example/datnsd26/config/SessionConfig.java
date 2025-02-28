package com.example.datnsd26.config;

import com.example.datnsd26.models.GioHang;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.SessionScope;

@Configuration
public class SessionConfig {
    @Bean
    @SessionScope
    public GioHang sessionCart() {
        GioHang gioHang = new GioHang();
        gioHang.setChiTietList(new java.util.ArrayList<>());
        gioHang.setTrangThai(false);
        return gioHang;
    }
}
