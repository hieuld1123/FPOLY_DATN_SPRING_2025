package com.example.datnsd26.services.impl;

import com.example.datnsd26.models.SanPhamChiTiet;
import com.example.datnsd26.repository.SanPhamChiTietRepository;
import com.example.datnsd26.services.SanPhamChiTietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SanPhamChiTietImp implements SanPhamChiTietService {
    @Autowired
    SanPhamChiTietRepository sanPhamChiTietRepository;


    @Override
    public List<SanPhamChiTiet> timSPCTHDCT(String tenSp, String chatlieu, String ThuongHieu,
                                            String De, String KichCo, String MauSac, Boolean gioitinh, BigDecimal gia) {
        return sanPhamChiTietRepository.searchSPCT(tenSp,chatlieu,ThuongHieu,De,
                KichCo,MauSac,gioitinh,gia);
    }

    @Override
    public List<SanPhamChiTiet> findAll() {
        return sanPhamChiTietRepository.findAll();
    }

    @Override
    public Page<SanPhamChiTiet> finAllPage(Integer soluong,Pageable pageable) {
        return sanPhamChiTietRepository.findAllBySoLuongGreaterThan(soluong,pageable);
    }

    @Override
    public SanPhamChiTiet addSPCT(SanPhamChiTiet sanPhamChiTiet) {
        return sanPhamChiTietRepository.save(sanPhamChiTiet);
    }

    @Override
    public void deleteSPCT(Integer id) {
        sanPhamChiTietRepository.deleteById(id);
    }

    @Override
    public List<SanPhamChiTiet> findBySanPhamId(Integer Id) {
        return sanPhamChiTietRepository.findBySanPhamId(Id);
    }

//    @Override
//    public void updateSoLuongVaGiaTien(List<Integer> ids, Integer soluong, BigDecimal giatien) {
//        sanPhamChiTietRepository.updateSoLuongVaGiaTien(ids,soluong,giatien);
//    }

//    @Override
//    public void update(Integer id, Integer soLuong, BigDecimal giaTien) {
//        sanPhamChiTietRepository.update(id,soLuong,giaTien);
//    }


    @Override
    public SanPhamChiTiet findById(Integer id) {
       return sanPhamChiTietRepository.findById(id).orElse(null);
    }

    @Override
    public List<SanPhamChiTiet> findBySanPhambyMa(String idSanPham) {
        return sanPhamChiTietRepository.searchSPCTtheoMa(idSanPham);
    }

    @Override
    public Boolean checkSPQR(String ma) {
        return sanPhamChiTietRepository.existsByMaSanPhamChiTiet(ma);
    }

}
