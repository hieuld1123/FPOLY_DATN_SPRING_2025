package com.example.datnsd26.services.impl;

import com.example.datnsd26.exception.NotFoundException;
import com.example.datnsd26.models.DeGiay;
import com.example.datnsd26.repository.DeGiayRepository;
import com.example.datnsd26.services.DeGiayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeGiayImp implements DeGiayService {
    @Autowired
    DeGiayRepository deGiayRepository;

    @Override
    public List<DeGiay> findAll() {
        return deGiayRepository.findAll();
    }

    @Override
    public DeGiay add(DeGiay deGiay) {
        return deGiayRepository.save(deGiay);
    }

    @Override
    public DeGiay findById(Integer id) {
        return deGiayRepository.findById(id).orElseThrow(() -> new NotFoundException("id không tồn tại"));
    }

    @Override
    public void delete(Integer id) {
        if (id == null) {
            throw new NotFoundException("ID không tồn tại");
        }
        deGiayRepository.deleteById(id);
    }

    @Override
    public List<DeGiay> getDeGiayByTen(String ten) {
        return deGiayRepository.findDeGiayByTenAndTrangThaiFalse(ten);
    }


}
