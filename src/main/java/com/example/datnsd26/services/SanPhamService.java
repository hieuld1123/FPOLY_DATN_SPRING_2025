package com.example.datnsd26.services;

import com.example.datnsd26.Dto.SanPhamSapHetDto;
import com.example.datnsd26.Dto.SanPhamThongKeDTO;
import com.example.datnsd26.models.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SanPhamService {

    List<SanPham> findAll();

    Page<SanPham> getAll(Pageable pageable);

//    Page<SanPham> findAllByTensanphamOrTrangthai(String tensanpham, Boolean trangthai, Pageable pageable);

    SanPham add(SanPham sanPham);
    SanPham findById(Integer Id);

    List<SanPhamThongKeDTO> layTopSanPhamBanChay(int limit);
    List<SanPhamSapHetDto> layTopSanPhamSapHet(int page, int pageSize);

}

