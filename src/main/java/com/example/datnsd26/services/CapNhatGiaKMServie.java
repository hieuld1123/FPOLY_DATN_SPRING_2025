package com.example.datnsd26.services;

import com.example.datnsd26.models.KhuyenMaiChitiet;
import com.example.datnsd26.models.SanPhamChiTiet;
import com.example.datnsd26.repository.KhuyenMaiChiTietRepository;
import com.example.datnsd26.repository.SanPhamChiTietRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CapNhatGiaKMServie {

    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final KhuyenMaiChiTietRepository khuyenMaiChiTietRepository;

    public CapNhatGiaKMServie(SanPhamChiTietRepository sanPhamChiTietRepository, KhuyenMaiChiTietRepository khuyenMaiChiTietRepository) {
        this.sanPhamChiTietRepository = sanPhamChiTietRepository;
        this.khuyenMaiChiTietRepository = khuyenMaiChiTietRepository;
    }

    @Transactional
    public void capNhatGiaSanPham() {
        LocalDateTime now = LocalDateTime.now();
        List<SanPhamChiTiet> danhSachSanPham = sanPhamChiTietRepository.findAll();

        for (SanPhamChiTiet sp : danhSachSanPham) {
            // Lấy khuyến mãi đang active của sản phẩm
            KhuyenMaiChitiet kmct = khuyenMaiChiTietRepository
                    .findActivePromotionBySanPham(Long.valueOf(sp.getId()), now);

            Float giaSauGiam = sp.getGiaBan();

            // Áp dụng khuyến mãi nếu có và đang active
            if (kmct != null && kmct.getKhuyenMai().getTrangThai() == 1) {
                if ("Phần Trăm".equals(kmct.getKhuyenMai().getHinhThucGiam())) {
                    giaSauGiam = sp.getGiaBan() - (sp.getGiaBan() * kmct.getSoTienGiam() / 100);
                } else {
                    giaSauGiam = sp.getGiaBan() - kmct.getSoTienGiam();
                }
                giaSauGiam = Math.max(giaSauGiam, sp.getGiaBan() * 0.05f); // Không giảm quá 95%
            }

            sp.setGiaBanSauGiam(giaSauGiam);
            sanPhamChiTietRepository.save(sp);
        }
    }
}
