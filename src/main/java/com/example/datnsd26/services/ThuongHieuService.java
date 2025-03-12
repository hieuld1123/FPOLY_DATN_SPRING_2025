package com.example.datnsd26.services;

import com.example.datnsd26.models.ThuongHieu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ThuongHieuService {
    Page<ThuongHieu> getAll(Pageable pageable);

    List<ThuongHieu> findAll();

    ThuongHieu add(ThuongHieu thuongHieu);

    ThuongHieu findById(Integer id);

    void delete(Integer id);

    List<ThuongHieu> getThuongHieuByTenOrTrangthai(String ten, Boolean trangthai);

}
