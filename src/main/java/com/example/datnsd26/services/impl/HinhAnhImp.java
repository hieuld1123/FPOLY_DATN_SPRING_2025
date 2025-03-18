package com.example.datnsd26.services.impl;

import com.example.datnsd26.models.HinhAnh;
import com.example.datnsd26.repository.HinhAnhRepository;
import com.example.datnsd26.services.HinhAnhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HinhAnhImp implements HinhAnhService {
    @Autowired
    HinhAnhRepository hinhAnhRepository;
    @Override
    public List<HinhAnh> findAll() {
        return hinhAnhRepository.findAll();
    }
}
