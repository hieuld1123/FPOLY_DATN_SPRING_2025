package com.example.datnsd26.services;

import com.example.datnsd26.models.ResourceNotFoundException;
import com.example.datnsd26.models.SanPhamChiTiet;
import com.example.datnsd26.repository.SanPhamChiTietRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SanPhamChitiet1 {
    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    public List<SanPhamChiTiet> findAll() {
        return sanPhamChiTietRepository.findAll();
    }

    public SanPhamChiTiet findById(Integer id) {
        return sanPhamChiTietRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SanPhamChiTiet", "id", id));
    }
}
