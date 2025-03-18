package com.example.datnsd26.services.impl;

import com.example.datnsd26.models.MauSac;
import com.example.datnsd26.repository.MauSacRepository;
import com.example.datnsd26.services.MauSacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MauSacImp implements MauSacService {
    @Autowired
    MauSacRepository mauSacRepository;

    @Override
    public List<MauSac> findAll() {
        return mauSacRepository.findAll();
    }

    @Override
    public MauSac addMauSac(MauSac mauSac) {
        return mauSacRepository.save(mauSac);
    }
}
