package com.example.datnsd26.services;

import com.example.datnsd26.models.Voucher;
import com.example.datnsd26.repository.VoucherRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherService {
    private final VoucherRepository voucherRepository;
    private final VoucherSchedulerService voucherSchedulerService;

    public List<Voucher> getAllVouchers() {
        List<Voucher> vouchers = voucherRepository.findAll();
        vouchers.forEach(v -> {
            if (v.getTrangThai() == null) {
                v.setTrangThai(0); // Mặc định là 0
            }
        });
        return vouchers;
    }

    public Optional<Voucher> getVoucherById(Long id) {
        return voucherRepository.findById(id);
    }

    public Voucher createVoucher(Voucher voucher) {
        Map<String, String> errors = new HashMap<>();
        validateVoucher(voucher, false, errors);
        if (!errors.isEmpty()) {
            throw new VoucherValidationException(errors);
        }

        voucher.setNgayTao(LocalDateTime.now());
        voucher.setNgayCapNhat(LocalDateTime.now());
        voucher.setTrangThai(determineVoucherStatus(voucher));
        Voucher saved = voucherRepository.save(voucher);

        // 🧠 Lên lịch tự động thay đổi trạng thái
        voucherSchedulerService.scheduleKhuyenMai(saved);

        return saved;
    }
    @Transactional
    public void updateVoucherTrangThaiById(Long id, int trangThai) {
        Voucher voucher = voucherRepository.findById(id).orElse(null);
        if (voucher != null) {
            voucher.setTrangThai(trangThai);
            voucher.setNgayCapNhat(LocalDateTime.now());
            voucherRepository.save(voucher);
            System.out.println("✅ Đã cập nhật trạng thái voucher ID " + id + " thành " + trangThai);
        }
    }


    public Voucher updateVoucher(Long id, Voucher newVoucher) {
        Map<String, String> errors = new HashMap<>();
        validateVoucher(newVoucher, true, errors);

        if (!errors.isEmpty()) {
            throw new VoucherValidationException(errors);
        }

        return voucherRepository.findById(id)
                .map(voucher -> {
                    voucher.setTenVoucher(newVoucher.getTenVoucher());
                    voucher.setHinhThucGiam(newVoucher.getHinhThucGiam());
                    voucher.setSoLuong(newVoucher.getSoLuong());
                    voucher.setGiaTriGiam(newVoucher.getGiaTriGiam());
                    voucher.setGiaTriGiamToiThieu(newVoucher.getGiaTriGiamToiThieu());
                    voucher.setGiaTriGiamToiDa(newVoucher.getGiaTriGiamToiDa());
                    voucher.setNgayBatDau(newVoucher.getNgayBatDau());
                    voucher.setNgayKetThuc(newVoucher.getNgayKetThuc());
                    voucher.setTrangThai(determineVoucherStatus(newVoucher));
                    Voucher updated = voucherRepository.save(voucher);

                    // 🔁 Cập nhật lại thời điểm thay đổi trạng thái
                    voucherSchedulerService.rescheduleKhuyenMai(updated);
                    return updated;
                })
                .orElseThrow(() -> new EntityNotFoundException("Voucher không tồn tại"));
    }

    public void deleteVoucher(Long id) {
        if (!voucherRepository.existsById(id)) {
            throw new EntityNotFoundException("Voucher không tồn tại với ID: " + id);
        }
        voucherRepository.deleteById(id);
        log.info("🗑 Voucher với ID {} đã bị xóa", id);
    }


    private Integer determineVoucherStatus(Voucher voucher) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getNgayBatDau())) {
            return 0; // Chưa bắt đầu
        } else if (now.isAfter(voucher.getNgayKetThuc())) {
            return 2; // Hết hạn
        } else {
            return 1; // Đang hoạt động
        }
    }

    private void validateVoucher(Voucher voucher, boolean isUpdate, Map<String, String> errors) {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0); // Làm tròn về phút
        LocalDateTime ngayBatDau = voucher.getNgayBatDau() != null ? voucher.getNgayBatDau().withSecond(0).withNano(0) : null;
        LocalDateTime ngayKetThuc = voucher.getNgayKetThuc() != null ? voucher.getNgayKetThuc().withSecond(0).withNano(0) : null;

        // Kiểm tra mã voucher
        if (voucher.getMaVoucher() == null || voucher.getMaVoucher().trim().isEmpty()) {
            errors.put("maVoucher", "Mã voucher không được để trống");
        } else if (!isUpdate && voucherRepository.findByMaVoucher(voucher.getMaVoucher()).isPresent()) {
            errors.put("maVoucher", "Mã voucher đã tồn tại, vui lòng nhập mã khác");
        }

        // Kiểm tra tên voucher
        if (voucher.getTenVoucher() == null || voucher.getTenVoucher().trim().isEmpty()) {
            errors.put("tenVoucher", "Tên voucher không được để trống");
        }

        if (voucher.getSoLuong() == null) {
            errors.put("soLuong", "Số lượng không được để trống");
        } else {
            try {
                Integer soLuong = voucher.getSoLuong(); // Ép kiểu để đảm bảo là số nguyên
                if (soLuong <= 0) {
                    errors.put("soLuong", "số lượng phải lớn hơn 0");
                }
            } catch (NumberFormatException e) {
                errors.put("soLuong", "Số lượng phải là một số hợp lệ");
            }
        }
        // Kiểm tra giá trị giảm
        if (voucher.getGiaTriGiam() == null) {
            errors.put("giaTriGiam", "Giá trị giảm không được để trống");
        } else {
            try {
                Double giatrigiam = voucher.getGiaTriGiam(); // Ép kiểu để đảm bảo là số nguyên
                if (giatrigiam <= 0) {
                    errors.put("giaTriGiam", "giá trị giảm phải lớn hơn 0");
                }
            } catch (NumberFormatException e) {
                errors.put("giaTriGiam", "giá trị giảm phải là một số hợp lệ");
            }
        }

        // Kiểm tra giá trị giảm tối đa
        if ("Phần Trăm".equalsIgnoreCase(voucher.getHinhThucGiam())) {
            if (voucher.getGiaTriGiamToiDa() == null) {
                errors.put("giaTriGiamToiDa", "Giá trị giảm tối đa không được để trống");
            } else {
                try {
                    Double giatrigiamtoida = voucher.getGiaTriGiamToiDa();
                    if (giatrigiamtoida <= 0) {
                        errors.put("giaTriGiamToiDa", "Giá trị giảm tối đa phải lớn hơn 0");
                    }
                } catch (NumberFormatException e) {
                    errors.put("giaTriGiamToiDa", "Giá trị giảm tối đa phải là một số hợp lệ");
                }
            }
        }

        // Kiểm tra giá trị giảm tối thiểu
        if (voucher.getGiaTriGiamToiThieu() == null) {
            errors.put("giaTriGiamToiThieu", "Đơn tối thiểu không được để trống");
        }else {
            try {
                Double giatrigiamtoithieu = voucher.getGiaTriGiamToiThieu(); // Ép kiểu để đảm bảo là số nguyên
                if (giatrigiamtoithieu < 0) {
                    errors.put("giaTriGiamToiThieu", "giá trị giảm tối thiểu phải lớn hơn hoăc bằng 0");
                }
            } catch (NumberFormatException e) {
                errors.put("giaTriGiamToiThieu", "giá trị giảm tối thiểu phải là một số hợp lệ");
            }
        }


        // Kiểm tra ngày bắt đầu & ngày kết thúc
        if (ngayBatDau == null) {
            errors.put("ngayBatDau", "Vui Lòng chọn Ngày bắt đầu ");
        } else if (!isUpdate || !ngayBatDau.equals(voucherRepository.findById(voucher.getId()).get().getNgayBatDau())) {
            if (ngayBatDau.isBefore(now)) {
                errors.put("ngayBatDau", "Ngày bắt đầu phải từ thời điểm hiện tại trở đi");
            }
        }

        if (ngayKetThuc == null) {
            errors.put("ngayKetThuc", "Vui lòng chon Ngày kết thúc ");
        } else if (ngayBatDau != null && ngayKetThuc.isBefore(ngayBatDau)) {
            errors.put("ngayKetThuc", "Ngày kết thúc phải sau ngày bắt đầu");
        }


        // Kiểm tra giá trị giảm trong khoảng cho phép
        if (voucher.getGiaTriGiam() != null) {
            Double giaTriGiam = voucher.getGiaTriGiam();
            Double giaTriGiamToiThieu = voucher.getGiaTriGiamToiThieu();
            Double giaTriGiamToiDa = voucher.getGiaTriGiamToiDa();
            String hinhThucGiam = voucher.getHinhThucGiam(); // Lấy hình thức giảm giá

            if ("Phần Trăm".equalsIgnoreCase(hinhThucGiam)) {
                // Kiểm tra nếu giảm giá theo phần trăm
                if (giaTriGiam < 0 || giaTriGiam > 100) {
                    errors.put("giaTriGiam", "Giá trị giảm theo phần trăm phải từ 0% đến 100%");
                }
                if (giaTriGiamToiDa != null && giaTriGiamToiThieu != null) {
                    // Giới hạn giá trị giảm tối đa khi giảm theo %
                    Double soTienGiamToiDa = (giaTriGiamToiThieu * giaTriGiam) / 100;
                    if (soTienGiamToiDa > giaTriGiamToiDa) {
                        errors.put("giaTriGiam", "Giá trị giảm không được vượt quá " + giaTriGiamToiDa);
                    }
                }
            } else if ("Theo Giá Tiền".equalsIgnoreCase(hinhThucGiam)) {

                // Giảm giá theo số tiền
                if (giaTriGiamToiDa != null && giaTriGiam > giaTriGiamToiDa) {
                    errors.put("giaTriGiam", "Giá trị giảm không được lớn hơn " + giaTriGiamToiDa);
                }
            }
            if (giaTriGiamToiThieu != null && giaTriGiamToiThieu < 0) {
                errors.put("giaTriGiamToiThieu", "Đơn hàng tối thiểu phải lớn hơn hoặc bằng 0");
            } else if ("Theo Giá Tiền".equalsIgnoreCase(hinhThucGiam)) {
                if (giaTriGiam <= 0) {
                    errors.put("giaTriGiam", "Giá trị giảm phải lớn hơn 0");
                }
            }
        }

        // Kiểm tra ngày bắt đầu khi cập nhật (nếu bị thay đổi)
        if (isUpdate && voucher.getId() != null) {
            voucherRepository.findById(voucher.getId()).ifPresent(oldVoucher -> {
                LocalDateTime oldNgayBatDau = oldVoucher.getNgayBatDau().withSecond(0).withNano(0);
                if (!ngayBatDau.equals(oldNgayBatDau) && ngayBatDau.isBefore(now)) {
                    errors.put("ngayBatDau", "Ngày bắt đầu phải từ thời điểm hiện tại trở đi");
                }
            });
        }


    }

    public Page<Voucher> searchVouchers(String maVoucher,
                                        String tenVoucher,
                                        Integer trangThai,
                                        LocalDateTime ngayBatDau,
                                        LocalDateTime ngayKetThuc,
                                        Pageable pageable) {
        return voucherRepository.searchVouchers(
                Optional.ofNullable(maVoucher).orElse(""),
                Optional.ofNullable(tenVoucher).orElse(""),
                Optional.ofNullable(trangThai).orElse(null),
                Optional.ofNullable(ngayBatDau).orElse(null),
                Optional.ofNullable(ngayKetThuc).orElse(null),
                pageable
        );
    }

    public Double applyVoucherToHoaDon(Voucher voucher, Double tongTienHoaDon) {
        if (!isVoucherValid(voucher, tongTienHoaDon)) {
            throw new IllegalStateException("Voucher không hợp lệ hoặc không thể áp dụng cho đơn hàng này");
        }

        Double tienGiam = 0.0;
        if ("Phần Trăm".equalsIgnoreCase(voucher.getHinhThucGiam())) {
            tienGiam = (tongTienHoaDon * voucher.getGiaTriGiam()) / 100;
            // Giới hạn số tiền giảm tối đa
            tienGiam = Math.min(tienGiam, voucher.getGiaTriGiamToiDa());
        } else {
            tienGiam = voucher.getGiaTriGiam();
        }

        // Đảm bảo số tiền giảm không âm và không vượt quá tổng tiền
        return Math.min(Math.max(tienGiam, 0), tongTienHoaDon);
    }

    private boolean isVoucherValid(Voucher voucher, Double tongTienHoaDon) {
        LocalDateTime now = LocalDateTime.now();

        return voucher != null
                && voucher.getTrangThai() == 0
                && voucher.getSoLuong() > 0
                && tongTienHoaDon >= voucher.getGiaTriGiamToiThieu()
                && now.isAfter(voucher.getNgayBatDau())
                && now.isBefore(voucher.getNgayKetThuc());
    }

    public void useVoucher(Long voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy voucher"));

        if (voucher.getSoLuong() <= 0) {
            throw new IllegalStateException("Voucher đã hết lượt sử dụng");
        }

        voucher.setSoLuong(voucher.getSoLuong() - 1);
        voucherRepository.save(voucher);
    }
    public Page<Voucher> getAllVouchers(Pageable pageable) {
        return voucherRepository.findAll(pageable);
    }
}