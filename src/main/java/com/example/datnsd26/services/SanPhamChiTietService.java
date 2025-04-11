package com.example.datnsd26.services;

import com.example.datnsd26.models.SanPhamChiTiet;
import com.example.datnsd26.repository.SanPhamChiTietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface SanPhamChiTietService {
    List<SanPhamChiTiet> timSPCTHDCT(String tenSp, String chatlieu,
                                     String ThuongHieu, String De,
                                     String KichCo, String MauSac,
                                     Boolean gioitinh, BigDecimal gia);
    List<SanPhamChiTiet> findAll();

    Page<SanPhamChiTiet> finAllPage(Integer sl,Pageable pageable);

    SanPhamChiTiet addSPCT(SanPhamChiTiet sanPhamChiTiet);

    void deleteSPCT(Integer id);

//    void updateSoLuongVaGiaTien(List<Integer> ids, Integer soluong, BigDecimal giatien);

//    void update(Integer id, Integer soLuong, BigDecimal giaTien);
    List<SanPhamChiTiet> findBySanPhamId(Integer idSanPham);
    SanPhamChiTiet findById( Integer id);
    List<SanPhamChiTiet> findBySanPhambyMa(String idSanPham);
    Boolean checkSPQR(String ma);


}
