package com.example.datnsd26.services;

import com.example.datnsd26.models.KhuyenMai;
import com.example.datnsd26.models.KhuyenMaiChitiet;
import com.example.datnsd26.models.ResourceNotFoundException;
import com.example.datnsd26.models.SanPhamChiTiet;
import com.example.datnsd26.repository.KhuyenMaiRepository;
import com.example.datnsd26.repository.SanPhamChiTietRepository;
import com.example.datnsd26.repository.KhuyenMaiChiTietRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
public class KhuyenMaiService {
    private final KhuyenMaiRepository khuyenMaiRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final KhuyenMaiChiTietRepository khuyenMaiChiTietRepository;
    private final CapNhatGiaKMServie capNhatGiaKMServie;
    private final KhuyenMaiSchedulerService khuyenMaiSchedulerService;


    private boolean isKhuyenMaiHienTai(KhuyenMai khuyenMai, LocalDateTime now) {
        return khuyenMai.getTrangThai() == 1
                && now.isAfter(khuyenMai.getThoiGianBatDau())
                && now.isBefore(khuyenMai.getThoiGianKetThuc());
    }

    //phan trang
    public Page<KhuyenMai> findAll(Pageable pageable) {
        Page<KhuyenMai> khuyenMais = khuyenMaiRepository.findAll(pageable);
        khuyenMais.getContent().forEach(km -> {
            if (km.getTrangThai() == null) {
                km.setTrangThai(0);
            }
        });
        return khuyenMais;
    }

    public List<KhuyenMai> findAll() {
        List<KhuyenMai> khuyenMais = khuyenMaiRepository.findAll();
        khuyenMais.forEach(km -> {
            if (km.getTrangThai() == null) {
                km.setTrangThai(0);
            }
        });
        return khuyenMais;
    }

    public Page<KhuyenMai> searchKhuyenMai(String tenChienDich, Integer trangThai,
                                           LocalDateTime startDate, LocalDateTime endDate,
                                           Pageable pageable) {
        return khuyenMaiRepository.searchKhuyenMai(tenChienDich, trangThai, startDate, endDate, pageable);
    }



    @Transactional
    public KhuyenMai save(KhuyenMai khuyenMai, Map<Integer, Float> sanPhamGiamGia) {
        try {
            validateKhuyenMaisave(khuyenMai);
            validateSanPhamGiamGia(sanPhamGiamGia, khuyenMai.getHinhThucGiam());

            LocalDateTime now = LocalDateTime.now();
            khuyenMai.setNgayTao(now);
            khuyenMai.setNgayCapNhat(now);
            khuyenMai.setTrangThai(determineKhuyenMaiStatus(khuyenMai));
            KhuyenMai savedKhuyenMai = khuyenMaiRepository.saveAndFlush(khuyenMai);

            List<KhuyenMaiChitiet> danhSachKmct = new ArrayList<>();

            for (Map.Entry<Integer, Float> entry : sanPhamGiamGia.entrySet()) {
                Integer sanPhamId = entry.getKey();
                Float soTienGiam = entry.getValue();

                SanPhamChiTiet sp = sanPhamChiTietRepository.findById(Math.toIntExact(sanPhamId))
                        .orElseThrow(() -> new ResourceNotFoundException("SanPhamChiTiet", "id", sanPhamId));

                List<KhuyenMaiChitiet> existingPromotions = khuyenMaiChiTietRepository
                        .findAllActivePromotionsBySanPham(Long.valueOf(sp.getId()), now);

                Float mucGiamMoi = tinhMucGiamThucTe(sp, khuyenMai.getHinhThucGiam(), soTienGiam);
                boolean shouldAddPromotion = true;

                for (KhuyenMaiChitiet existingKmct : existingPromotions) {
                    Float mucGiamHienTai = tinhMucGiamThucTe(sp,
                            existingKmct.getKhuyenMai().getHinhThucGiam(),
                            existingKmct.getSoTienGiam());

                    if (mucGiamHienTai >= mucGiamMoi) {
                        shouldAddPromotion = false;
                        break;
                    } else {
                        khuyenMaiChiTietRepository.delete(existingKmct);
                    }
                }

                if (shouldAddPromotion) {
                    KhuyenMaiChitiet kmct = new KhuyenMaiChitiet();
                    kmct.setKhuyenMai(savedKhuyenMai);
                    kmct.setSanPhamChiTiet(sp);
                    kmct.setSoTienGiam(soTienGiam);
                    kmct.setTrangThai(savedKhuyenMai.getTrangThai());
                    danhSachKmct.add(kmct);
                }
            }

            if (!danhSachKmct.isEmpty()) {
                khuyenMaiChiTietRepository.saveAll(danhSachKmct);
            }



            capNhatGiaKMServie.capNhatGiaSanPham();
            khuyenMaiSchedulerService.scheduleKhuyenMai(savedKhuyenMai);
            return savedKhuyenMai;
        } catch (Exception e) {
            throw e;
        }
    }


    private Float tinhMucGiamThucTe(SanPhamChiTiet sp, String hinhThucGiam, Float soTienGiam) {
        if ("Phần Trăm".equals(hinhThucGiam)) {
            return sp.getGiaBan() * soTienGiam / 100;
        }
        return soTienGiam;
    }

    @Transactional
    protected void saveKhuyenMaiChiTiet(KhuyenMai khuyenMai, Map<Integer, Float> sanPhamGiamGia) {
        if (sanPhamGiamGia.isEmpty()) {
            throw new IllegalArgumentException("Không có sản phẩm nào được chọn để giảm giá.");
        }

        List<KhuyenMaiChitiet> danhSachKmct = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<Integer, Float> entry : sanPhamGiamGia.entrySet()) {
            Integer sanPhamId = entry.getKey();
            Float soTienGiam = entry.getValue();

            SanPhamChiTiet sp = sanPhamChiTietRepository.findById(sanPhamId)
                    .orElseThrow(() -> new ResourceNotFoundException("SanPhamChiTiet", "id", sanPhamId));

            // Kiểm tra các khuyến mãi hiện có
            List<KhuyenMaiChitiet> existingPromotions = khuyenMaiChiTietRepository
                    .findAllActivePromotionsBySanPham(Long.valueOf(sp.getId()), now);

            Float mucGiamMoi = tinhMucGiamThucTe(sp, khuyenMai.getHinhThucGiam(), soTienGiam);
            boolean shouldAddPromotion = true;

            for (KhuyenMaiChitiet existingKmct : existingPromotions) {
                if (!existingKmct.getKhuyenMai().getId().equals(khuyenMai.getId())) {
                    Float mucGiamHienTai = tinhMucGiamThucTe(sp,
                            existingKmct.getKhuyenMai().getHinhThucGiam(),
                            existingKmct.getSoTienGiam());

                    if (mucGiamHienTai >= mucGiamMoi) {
                        shouldAddPromotion = false;
                        break;
                    } else {
                        khuyenMaiChiTietRepository.delete(existingKmct);
                    }
                }
            }

            if (shouldAddPromotion) {
                KhuyenMaiChitiet kmct = new KhuyenMaiChitiet(khuyenMai, sp, soTienGiam, khuyenMai.getTrangThai());
                danhSachKmct.add(kmct);
            }
        }

        if (!danhSachKmct.isEmpty()) {
            khuyenMaiChiTietRepository.saveAll(danhSachKmct);
        }
    }


    @Transactional
    public KhuyenMai update(Long id, KhuyenMai khuyenMaiMoi, Map<Integer, Float> sanPhamGiamGia) {
        KhuyenMai khuyenMaiHienTai = khuyenMaiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KhuyenMai", "id", id));

        // Cập nhật thông tin cơ bản
        if (khuyenMaiMoi.getTenChienDich() != null) {
            khuyenMaiHienTai.setTenChienDich(khuyenMaiMoi.getTenChienDich());
        }

        // Cập nhật thời gian
        if (khuyenMaiMoi.getThoiGianBatDau() != null && khuyenMaiMoi.getThoiGianKetThuc() != null) {
            if (khuyenMaiMoi.getThoiGianBatDau().isAfter(khuyenMaiMoi.getThoiGianKetThuc())) {
                throw new IllegalArgumentException("Thời gian bắt đầu phải trước thời gian kết thúc");
            }
            khuyenMaiHienTai.setThoiGianBatDau(khuyenMaiMoi.getThoiGianBatDau());
            khuyenMaiHienTai.setThoiGianKetThuc(khuyenMaiMoi.getThoiGianKetThuc());
        }

        if (khuyenMaiMoi.getHinhThucGiam() != null) {
            khuyenMaiHienTai.setHinhThucGiam(khuyenMaiMoi.getHinhThucGiam());
        }

        if (khuyenMaiMoi.getGiaTriGiam() != null) {
            khuyenMaiHienTai.setGiaTriGiam(khuyenMaiMoi.getGiaTriGiam());
        }

        khuyenMaiHienTai.setNgayCapNhat(LocalDateTime.now());
        khuyenMaiHienTai.setTrangThai(determineKhuyenMaiStatus(khuyenMaiHienTai));

        // Lưu khuyến mãi cập nhật
        KhuyenMai savedKhuyenMai = khuyenMaiRepository.save(khuyenMaiHienTai);

        // Cập nhật chi tiết khuyến mãi nếu có danh sách sản phẩm
        if (sanPhamGiamGia != null && !sanPhamGiamGia.isEmpty()) {
            validateSanPhamGiamGia(sanPhamGiamGia, savedKhuyenMai.getHinhThucGiam());

            List<KhuyenMaiChitiet> danhSachKmctMoi = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            for (Map.Entry<Integer, Float> entry : sanPhamGiamGia.entrySet()) {
                Integer sanPhamId = entry.getKey();
                Float soTienGiam = entry.getValue();

                SanPhamChiTiet sp = sanPhamChiTietRepository.findById(sanPhamId)
                        .orElseThrow(() -> new ResourceNotFoundException("SanPhamChiTiet", "id", sanPhamId));

                Float mucGiamMoi = tinhMucGiamThucTe(sp, savedKhuyenMai.getHinhThucGiam(), soTienGiam);

                List<KhuyenMaiChitiet> kmctHienTai = khuyenMaiChiTietRepository
                        .findAllActivePromotionsBySanPham(sp.getId().longValue(), now);

                boolean coKhuyenMaiTotHon = false;

                for (KhuyenMaiChitiet kmct : kmctHienTai) {
                    if (!kmct.getKhuyenMai().getId().equals(id)) {
                        Float mucGiamHienTai = tinhMucGiamThucTe(sp,
                                kmct.getKhuyenMai().getHinhThucGiam(),
                                kmct.getSoTienGiam());

                        if (mucGiamHienTai >= mucGiamMoi) {
                            coKhuyenMaiTotHon = true;
                            break;
                        }
                    }
                }

                if (!coKhuyenMaiTotHon) {
                    KhuyenMaiChitiet kmctMoi = new KhuyenMaiChitiet(
                            savedKhuyenMai,
                            sp,
                            soTienGiam,
                            savedKhuyenMai.getTrangThai()
                    );
                    danhSachKmctMoi.add(kmctMoi);
                }
            }

            // Xóa chi tiết cũ và lưu chi tiết mới
            khuyenMaiChiTietRepository.deleteByKhuyenMai_Id(id);
            if (!danhSachKmctMoi.isEmpty()) {
                khuyenMaiChiTietRepository.saveAll(danhSachKmctMoi);
            }
        }

        capNhatGiaKMServie.capNhatGiaSanPham();
        khuyenMaiSchedulerService.rescheduleKhuyenMai(savedKhuyenMai);
        return savedKhuyenMai;
    }


    private int determineKhuyenMaiStatus(KhuyenMai khuyenMai) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(khuyenMai.getThoiGianBatDau())) {
            return 0; // Chưa bắt đầu
        } else if (now.isAfter(khuyenMai.getThoiGianKetThuc())) {
            return 2; // Đã kết thúc
        } else {
            return 1; // Đang hoạt động
        }
    }


    @Transactional
    public void delete(Long id) {
        KhuyenMai km = khuyenMaiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KhuyenMai", "id", id));

        khuyenMaiChiTietRepository.deleteByKhuyenMai_Id(id);
        khuyenMaiRepository.deleteById(id);
        capNhatGiaKMServie.capNhatGiaSanPham();
    }


    private void validateKhuyenMaisave(KhuyenMai khuyenMai) {
        if (khuyenMai.getTenChienDich() == null || khuyenMai.getTenChienDich().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên chiến dịch không được để trống");
        }

        if (khuyenMai.getThoiGianBatDau() == null || khuyenMai.getThoiGianKetThuc() == null) {
            throw new IllegalArgumentException("Thời gian bắt đầu và kết thúc không được để trống");
        }

        if (khuyenMai.getThoiGianBatDau().isAfter(khuyenMai.getThoiGianKetThuc())) {
            throw new IllegalArgumentException("Thời gian bắt đầu phải trước thời gian kết thúc");
        }

        if (!"Phần Trăm".equals(khuyenMai.getHinhThucGiam()) &&
                !"Theo Giá Tiền".equals(khuyenMai.getHinhThucGiam())) {
            throw new IllegalArgumentException("Hình thức giảm không hợp lệ");
        }


    }

    public KhuyenMai findById(Long id) {
        return khuyenMaiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KhuyenMai", "id", id));
    }

    public List<Long> getSelectedProductIds(Long khuyenMaiId) {
        return khuyenMaiChiTietRepository.findByKhuyenMai_Id(khuyenMaiId)
                .stream()
                .map(kmct -> Long.valueOf(kmct.getSanPhamChiTiet().getId()))
                .toList();
    }


    public Map<Integer, Float> getGiaTriGiamMap(Long khuyenMaiId) {
        List<KhuyenMaiChitiet> khuyenMaiChitiets = khuyenMaiChiTietRepository.findByKhuyenMai_IdOrderBySanPhamChiTietAsc(khuyenMaiId);
        Map<Integer, Float> giaTriGiamMap = new HashMap<>();

        for (KhuyenMaiChitiet kmct : khuyenMaiChitiets) {
            if (kmct.getSanPhamChiTiet() != null) {
                giaTriGiamMap.put(
                        kmct.getSanPhamChiTiet().getId(),
                        kmct.getSoTienGiam()
                );
            }
        }

        return giaTriGiamMap;
    }

    public Page<SanPhamChiTiet> finAllPage(Pageable pageable) {
        return sanPhamChiTietRepository.findAll(pageable);
    }

    public void restoreKhuyenMai(Long id) {
        KhuyenMai khuyenMai = findById(id);
        if (khuyenMai.getTrangThai() != 2) {
            throw new IllegalStateException("Chỉ có thể khôi phục khuyến mãi đã kết thúc");
        }
        khuyenMai.setTrangThai(1);
        khuyenMai.setNgayCapNhat(LocalDateTime.now());
        khuyenMaiRepository.save(khuyenMai);
        capNhatGiaKMServie.capNhatGiaSanPham();
    }

    public void stopKhuyenMai(Long id) {
        KhuyenMai khuyenMai = findById(id);
        if (khuyenMai.getTrangThai() != 1) {
            throw new IllegalStateException("Chỉ có thể ngừng khuyến mãi đang hoạt động");
        }
        khuyenMai.setTrangThai(2);
        khuyenMai.setNgayCapNhat(LocalDateTime.now());
        khuyenMaiRepository.save(khuyenMai);
        capNhatGiaKMServie.capNhatGiaSanPham();
    }

    private void validateSanPhamGiamGia(Map<Integer, Float> sanPhamGiamGia, String hinhThucGiam) {
        if (sanPhamGiamGia == null || sanPhamGiamGia.isEmpty()) {
            throw new IllegalArgumentException("Phải chọn ít nhất một sản phẩm để áp dụng khuyến mãi");
        }

        for (Map.Entry<Integer, Float> entry : sanPhamGiamGia.entrySet()) {
            Float giaTriGiam = entry.getValue();
            if (giaTriGiam == null || giaTriGiam <= 0) {
                throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0");
            }

            if ("Phần Trăm".equals(hinhThucGiam) && giaTriGiam > 100) {
                throw new IllegalArgumentException("Giảm giá theo phần trăm không được vượt quá 100%");
            }

            SanPhamChiTiet sp = sanPhamChiTietRepository.findById(Math.toIntExact(entry.getKey()))
                    .orElseThrow(() -> new ResourceNotFoundException("SanPhamChiTiet", "id", entry.getKey()));

            if ("Theo Giá Tiền".equals(hinhThucGiam) && giaTriGiam >= sp.getGiaBan()) {
                throw new IllegalArgumentException("Giá trị giảm không được lớn hơn hoặc bằng giá bán của sản phẩm");
            }
        }
    }

    public List<SanPhamChiTiet> getSanPhamDaApDungKhac(Long khuyenMaiId) {
        LocalDateTime now = LocalDateTime.now();
        List<SanPhamChiTiet> allSanPhams = sanPhamChiTietRepository.findAll();
        List<SanPhamChiTiet> sanPhamDaApDungKhac = new ArrayList<>();

        for (SanPhamChiTiet sp : allSanPhams) {
            // Lấy khuyến mãi đang active của sản phẩm
            KhuyenMaiChitiet kmct = khuyenMaiChiTietRepository.findActivePromotionBySanPham(Long.valueOf(sp.getId()), now);
            if (kmct != null && kmct.getKhuyenMai().getTrangThai() == 1
                    && !kmct.getKhuyenMai().getId().equals(khuyenMaiId)) {
                sanPhamDaApDungKhac.add(sp);
            }
        }
        return sanPhamDaApDungKhac;
    }


    public void deleteById(Long id) {
    }
}