package com.example.datnsd26.services;


import com.example.datnsd26.models.TaiKhoan;
import com.example.datnsd26.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaiKhoanService {
    @Autowired
    TaiKhoanRepository taiKhoanRepository;


    public List<TaiKhoan> getAll() {
        return taiKhoanRepository.findAll();
    }

    public TaiKhoan findByEmail(String email) {
        return taiKhoanRepository.findByEmail(email).orElse(null);
    }

    public TaiKhoan save(TaiKhoan taiKhoan){
        return taiKhoanRepository.save(taiKhoan);
    }
}
