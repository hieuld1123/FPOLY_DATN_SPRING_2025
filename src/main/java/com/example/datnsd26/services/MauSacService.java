package com.example.datnsd26.services;

import com.example.datnsd26.models.MauSac;

import java.util.List;

public interface MauSacService {
    List<MauSac> findAll();

    MauSac addMauSac(MauSac mauSac);
}
