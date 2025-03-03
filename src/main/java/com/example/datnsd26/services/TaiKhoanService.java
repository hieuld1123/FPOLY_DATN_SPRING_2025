package com.example.datnsd26.services;


import com.example.datnsd26.models.TaiKhoan;
import com.example.datnsd26.repository.TaiKhoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaiKhoanService {
    @Autowired
    TaiKhoanRepository taiKhoanRepository;

    public List<TaiKhoan> getAll() {
        return taiKhoanRepository.findAll();
    }

}
