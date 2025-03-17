package com.example.datnsd26.services;

import com.example.datnsd26.models.DeGiay;

import java.util.List;


public interface DeGiayService {

    List<DeGiay> findAll();

    DeGiay add(DeGiay deGiay);

    DeGiay findById(Integer id);

    void delete(Integer id);

    List<DeGiay> getDeGiayByTen(String ten);


}
