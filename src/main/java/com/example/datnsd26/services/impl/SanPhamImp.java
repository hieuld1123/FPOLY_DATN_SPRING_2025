package com.example.datnsd26.services.impl;

import com.example.datnsd26.Dto.SanPhamSapHetDto;
import com.example.datnsd26.Dto.SanPhamThongKeDTO;
import com.example.datnsd26.models.SanPham;
import com.example.datnsd26.repository.HoaDonChiTietRepository;
import com.example.datnsd26.repository.SanPhamChiTietRepository;
import com.example.datnsd26.repository.SanPhamRepositoty;
import com.example.datnsd26.services.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SanPhamImp implements SanPhamService {
    @Autowired
    SanPhamRepositoty sanPhamRepositoty;

    @Autowired
    HoaDonChiTietRepository hoaDonChiTietRepository;
    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;


    @Override
    public List<SanPham> findAll() {
        return sanPhamRepositoty.findAll();
    }

    @Override
    public Page<SanPham> getAll(Pageable pageable) {
        return sanPhamRepositoty.findAll(pageable);
    }

//    @Override
//    public Page<SanPham> findAllByTensanphamOrTrangthai(String tensanpham, Boolean trangthai, Pageable pageable) {
//        return sanPhamRepositoty.findAllByTensanphamOrTrangthai(tensanpham,trangthai,pageable);
//    }

    @Override
    public SanPham add(SanPham sanPham) {
        return sanPhamRepositoty.save(sanPham);
    }

    @Override
    public SanPham findById(Integer Id) {
        return sanPhamRepositoty.getById(Id);
    }

    @Override
    public List<SanPhamThongKeDTO> layTopSanPhamBanChay(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> rawData = hoaDonChiTietRepository.findTopSanPhamThongKe(pageable);

        List<SanPhamThongKeDTO> result = new ArrayList<>();

        for (Object[] row : rawData) {
            String maSP = (String) row[1];
            String tenSP = (String) row[2];
            Long soLuong = ((Number) row[3]).longValue();
            Double doanhThu = ((Number) row[4]).doubleValue();

            result.add(new SanPhamThongKeDTO(maSP, tenSP, soLuong, doanhThu));
        }

        return result;
    }

    @Override
    public List<SanPhamSapHetDto> layTopSanPhamSapHet() {
        List<Object[]> results = sanPhamChiTietRepository.getTopSanPhamSapHetNative();
        return results.stream().map(obj -> new SanPhamSapHetDto(
                (String) obj[0],                            // maSanPham
                (String) obj[1],                            // tenSanPham
                ((Number) obj[2]).intValue(),               // tongSoLuongTon
                (String) obj[3]                             // anhDaiDien
        )).collect(Collectors.toList());
    }


    public void updateProductStatus(Integer id, boolean trangthai) {
        SanPham sanPham = sanPhamRepositoty.findById(id).orElse(null);
        sanPham.setTrangThai(trangthai);
        sanPhamRepositoty.save(sanPham);
    }


}
