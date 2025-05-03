package com.example.datnsd26.services;

import com.example.datnsd26.models.KhuyenMai;
import com.example.datnsd26.models.ResourceNotFoundException;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@Transactional
public class VoucherService {
    private final VoucherRepository voucherRepository;
    private final VoucherSchedulerService voucherSchedulerService;
    private  final VoucherStatusUpdater voucherStatusUpdater;


    public Page<Voucher> getAllVouchers(Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "ngayTao")
        );
        Page<Voucher> page = voucherRepository.findAll(sortedPageable);

        page.getContent().forEach(v -> {
            if (v.getTrangThai() == null) {
                v.setTrangThai(0); // Mặc định là 0
            }
        });

        return page;
    }

    public Optional<Voucher> getVoucherById(Long id) {
        return voucherRepository.findById(id);
    }

    private String generateVoucherCode() {
        String prefix = "VC";
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder randomString = new StringBuilder();
        int length = 8; // Độ dài chuỗi random

        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            randomString.append(chars.charAt(index));
        }

        return prefix + randomString.toString();
    }

    public Voucher createVoucher(Voucher voucher) {
        Map<String, String> errors = new HashMap<>();

        if (voucher.getMaVoucher() == null || voucher.getMaVoucher().trim().isEmpty()) {
            String newCode;
            do {
                newCode = generateVoucherCode();
            } while (voucherRepository.findByMaVoucher(newCode).isPresent());
            voucher.setMaVoucher(newCode);
        }

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

        if (voucher.getTenVoucher() == null || voucher.getTenVoucher().trim().isEmpty()) {
            errors.put("tenVoucher", "Tên voucher không được để trống");
        } else if (!isUpdate) {
            List<Voucher> vouchers = voucherRepository.findByTenVoucher(voucher.getTenVoucher());
            if (!vouchers.isEmpty()) {
                errors.put("tenVoucher", "Tên voucher đã tồn tại");
            }
        } else {
            // Khi update, chỉ kiểm tra trùng tên với các voucher khác
            List<Voucher> vouchers = voucherRepository.findByTenVoucherAndIdNot(voucher.getTenVoucher(), voucher.getId());
            if (!vouchers.isEmpty()) {
                errors.put("tenVoucher", "Tên voucher đã tồn tại");
            }
        }

        // Độ dài tên phải từ 3 đến 255 ký tự
        if (voucher.getTenVoucher().length() < 3 || voucher.getTenVoucher().length() > 255) {
            errors.put("tenVoucher", "Tên voucher phải từ 3 đến 255 ký tự");
        }


        // Kiểm tra số lượng
        if (voucher.getSoLuong() == null) {
            errors.put("soLuong", "Số lượng không được để trống");
        } else {
            try {
                Integer soLuong = voucher.getSoLuong();
                if (soLuong <= 0) {
                    errors.put("soLuong", "Số lượng phải lớn hơn 0");
                } else if (soLuong > 100000) {
                    errors.put("soLuong", "Số lượng không được vượt quá 1 trăm nghìn");
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
                Float giaTriGiam = voucher.getGiaTriGiam();
                if (giaTriGiam <= 0) {
                    errors.put("giaTriGiam", "Giá trị giảm phải lớn hơn 0");
                } else if (giaTriGiam > 1_000_000_000) {
                    errors.put("giaTriGiam", "Giá trị giảm không được vượt quá 1 tỷ");
                }
            } catch (NumberFormatException e) {
                errors.put("giaTriGiam", "Giá trị giảm phải là một số hợp lệ");
            }
        }

// Kiểm tra giá trị giảm tối đa (chỉ khi giảm theo phần trăm)
        if ("Phần Trăm".equalsIgnoreCase(voucher.getHinhThucGiam())) {
            if (voucher.getGiaTriGiamToiDa() == null) {
                errors.put("giaTriGiamToiDa", "Giá trị giảm tối đa không được để trống");
            } else {
                try {
                    Float giamToiDa = voucher.getGiaTriGiamToiDa();
                    if (giamToiDa <= 0) {
                        errors.put("giaTriGiamToiDa", "Giá trị giảm tối đa phải lớn hơn 0");
                    } else if (giamToiDa > 1_000_000_000) {
                        errors.put("giaTriGiamToiDa", "Giá trị giảm tối đa không được vượt quá 1 tỷ");
                    }
                } catch (NumberFormatException e) {
                    errors.put("giaTriGiamToiDa", "Giá trị giảm tối đa phải là một số hợp lệ");
                }
            }
        }
// Kiểm tra giá trị giảm tối thiểu (đơn tối thiểu)
        if (voucher.getGiaTriGiamToiThieu() == null) {
            errors.put("giaTriGiamToiThieu", "Đơn tối thiểu không được để trống");
        } else {
            try {
                Float giamToiThieu = voucher.getGiaTriGiamToiThieu();
                if (giamToiThieu < 0) {
                    errors.put("giaTriGiamToiThieu", "Đơn tối thiểu không được âm");
                } else if (giamToiThieu > 1_000_000_000) {
                    errors.put("giaTriGiamToiThieu", "Đơn tối thiểu không được vượt quá 1 tỷ");
                }
            } catch (NumberFormatException e) {
                errors.put("giaTriGiamToiThieu", "Đơn tối thiểu phải là một số hợp lệ");
            }
        }
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
        Float giaTriGiam = voucher.getGiaTriGiam();
        Float donToiThieu = voucher.getGiaTriGiamToiThieu();
        Float giaTriGiamToiDa = voucher.getGiaTriGiamToiDa();
        String hinhThucGiam = voucher.getHinhThucGiam();

        if ("Theo Giá Tiền".equals(hinhThucGiam)) {
            // Validate x (Giá trị giảm)
            if(giaTriGiam != null){
                if (giaTriGiam <= 0) {
                    errors.put("giaTriGiam", "Giá trị giảm phải lớn hơn 0");
                }
                if (donToiThieu != null && giaTriGiam > donToiThieu) {
                    errors.put("giaTriGiam", "Giá trị giảm không được lớn hơn đơn tối thiểu");
                }
            }
            // Validate y (Đơn tối thiểu)
            if (donToiThieu == null || donToiThieu <= 0)  {
                errors.put("giaTriGiamToiThieu", "Đơn tối thiểu phải lớn hơn hoặc bằng 0");
            }
        }
        else if ("Phần Trăm".equals(hinhThucGiam)) {
            // Validate x (Giá trị giảm phần trăm)
            if (giaTriGiam == null || giaTriGiam < 0 || giaTriGiam > 100) {
                errors.put("giaTriGiam", "Giá trị giảm phải từ 1 đến 100%");
            }
            // Validate y (Đơn tối thiểu)
            if (donToiThieu == null || donToiThieu <= 0) {
                errors.put("giaTriGiamToiThieu", "Đơn tối thiểu phải lớn hơn hoặc bằng 0");
            }
            // Validate z (Giá trị giảm tối đa)
            if (giaTriGiamToiDa != null) {
                if (giaTriGiamToiDa < 0) {
                    errors.put("giaTriGiamToiDa", "Giá trị giảm tối đa phải lớn hơn 0");
                }
                if (donToiThieu != null && giaTriGiamToiDa > donToiThieu) {
                    errors.put("giaTriGiamToiDa", "Giá trị giảm tối đa không được lớn hơn đơn tối thiểu");
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
    public Voucher findById(Long id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher", "id", id));
    }

    // Đổi tên từ restoreKhuyenMai và stopKhuyenMai thành restoreVoucher và endVoucher
    @Transactional
    public void restoreVoucher(Long id) {
        Voucher voucher = findById(id);
        if (voucher.getTrangThai() != 2) {
            throw new IllegalStateException("Chỉ có thể khôi phục voucher đã kết thúc");
        }
        voucher.setTrangThai(1);
        voucher.setNgayCapNhat(LocalDateTime.now());
//        voucherStatusUpdater.updateVoucherStatus();
        voucherRepository.save(voucher);
    }

    @Transactional
    public void endVoucher(Long id) {
        Voucher voucher = findById(id);
        if (voucher.getTrangThai() != 1) {
            throw new IllegalStateException("Chỉ có thể kết thúc voucher đang hoạt động");
        }
        voucher.setTrangThai(2);
//        voucherStatusUpdater.updateVoucherStatus();
        voucher.setNgayCapNhat(LocalDateTime.now());
        voucherRepository.save(voucher);
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


    public void useVoucher(Long voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy voucher"));

        if (voucher.getSoLuong() <= 0) {
            throw new IllegalStateException("Voucher đã hết lượt sử dụng");
        }

        voucher.setSoLuong(voucher.getSoLuong() - 1);
        voucherRepository.save(voucher);
    }

    //Code cua Binh. Cam xoa
    public float tinhGiamGia(float tongTamTinh, Voucher voucher) {
        if (voucher == null || voucher.getSoLuong() <= 0 || voucher.getHinhThucGiam() == null) {
            return 0f;
        }

        float giamGia = 0f;

        String loai = voucher.getHinhThucGiam().trim().toLowerCase();

        if (loai.equals("phần trăm")) {
            giamGia = tongTamTinh * voucher.getGiaTriGiam() / 100f;
            if(giamGia >= voucher.getGiaTriGiamToiDa()) {
                giamGia = voucher.getGiaTriGiamToiDa();
            }
        } else if (loai.equals("theo giá tiền")) {
            giamGia = voucher.getGiaTriGiam();
        }

        return giamGia;
    }


}