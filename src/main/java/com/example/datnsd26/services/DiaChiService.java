package com.example.datnsd26.services;

import com.example.datnsd26.models.DiaChi;
import com.example.datnsd26.repository.DiaChiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiaChiService {
    @Autowired
    DiaChiRepository diaChiRepository;

    public List<DiaChi> findByKhachHangId(Integer id) {
        return diaChiRepository.findByKhachHangId(id);
    }
}
