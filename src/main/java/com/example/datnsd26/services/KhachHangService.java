package com.example.datnsd26.services;

import com.example.datnsd26.models.DiaChi;
import com.example.datnsd26.models.KhachHang;
import com.example.datnsd26.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KhachHangService {
    @Autowired
    private KhachHangRepository khachHangRepository;
    public List<KhachHang> getAll() {
        return khachHangRepository.findAll();
    }



}
