package com.example.datnsd26.services;

import com.example.datnsd26.models.KichCo;

import java.util.List;

public interface KichCoService {
    List<KichCo> findAll();

    KichCo addKichCo(KichCo kichCo);
}
