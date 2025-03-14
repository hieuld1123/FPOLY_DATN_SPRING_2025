package com.example.datnsd26.services.impl;

import com.example.datnsd26.exception.NotFoundException;
import com.example.datnsd26.models.ThuongHieu;
import com.example.datnsd26.repository.ThuongHieuRepository;
import com.example.datnsd26.services.ThuongHieuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThuongHieuImp implements ThuongHieuService {
    @Autowired
    ThuongHieuRepository thuongHieuRepository;

    @Override
    public List<ThuongHieu> findAll() {
        return thuongHieuRepository.findAll();
    }

    @Override
    public ThuongHieu add(ThuongHieu thuongHieu) {
        return thuongHieuRepository.save(thuongHieu);
    }

    @Override
    public ThuongHieu findById(Integer id) {
        return thuongHieuRepository.findById(id).orElseThrow(() -> new NotFoundException("id không tồn tại"));
    }

    @Override
    public void delete(Integer id) {
        thuongHieuRepository.deleteById(id);
    }



    @Override
    public Page<ThuongHieu> getAll(Pageable pageable) {
        return thuongHieuRepository.findAll(pageable);

    }

    @Override
    public List<ThuongHieu> getThuongHieuByTenOrTrangthai(String ten, Boolean trangthai) {
        return thuongHieuRepository.getThuongHieuByTenOrTrangThai(ten, trangthai);
    }
}
