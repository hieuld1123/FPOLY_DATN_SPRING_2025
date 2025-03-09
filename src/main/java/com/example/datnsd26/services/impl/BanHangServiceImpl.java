package com.example.datnsd26.services.impl;

import com.example.datnsd26.services.BanHangService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j(topic = "BAN-HANG-SERVICE")
public class BanHangServiceImpl implements BanHangService {


    @Override
    public void getHoaDon() {
        log.info("List Hoa Don....");
    }
}
