package com.example.datnsd26.services.impl;

import com.example.datnsd26.models.KichCo;
import com.example.datnsd26.repository.KichCoRepository;
import com.example.datnsd26.services.KichCoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KichCoImp implements KichCoService {
    @Autowired
    KichCoRepository kichCoRepository;
    @Override
    public List<KichCo> findAll() {
        return kichCoRepository.findAll();
    }

    @Override
    public KichCo addKichCo(KichCo kichCo) {
        return kichCoRepository.save(kichCo);
    }
}
