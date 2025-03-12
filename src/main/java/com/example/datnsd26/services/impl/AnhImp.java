package com.example.datnsd26.services.impl;

import com.example.datnsd26.models.Anh;
import com.example.datnsd26.repository.AnhRepository;
import com.example.datnsd26.services.AnhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnhImp implements AnhService {
    @Autowired
    AnhRepository anhRepository;
    @Override
    public List<Anh> findAll() {
        return anhRepository.findAll();
    }
}
