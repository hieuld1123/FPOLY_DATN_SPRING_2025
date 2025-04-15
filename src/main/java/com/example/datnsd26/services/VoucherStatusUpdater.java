package com.example.datnsd26.services;

import com.example.datnsd26.models.Voucher;
import com.example.datnsd26.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class VoucherStatusUpdater {

    private final VoucherRepository voucherRepository;

    @Transactional
    public void updateVoucherStatus() {
        LocalDateTime now = LocalDateTime.now();

        // Cập nhật trạng thái cho voucher đến thời gian bắt đầu
        List<Voucher> toActive = voucherRepository.findVouchersToActivate(now);
        for (Voucher voucher : toActive) {
            if (voucher.getSoLuong() > 0) {  // Chỉ active nếu còn số lượng
                voucherRepository.updateVoucherStatusById(voucher.getId(), 1, now);
            }
        }

        // Cập nhật trạng thái cho voucher hết số lượng hoặc hết hạn
        List<Voucher> activeVouchers = voucherRepository.findByTrangThai(1);
        for (Voucher voucher : activeVouchers) {
            if (voucher.getSoLuong() <= 0 || now.isAfter(voucher.getNgayKetThuc())) {
                voucherRepository.updateVoucherStatusById(voucher.getId(), 2, now);
            }
        }

        // Kiểm tra và cập nhật tất cả voucher có số lượng = 0
        List<Voucher> allActiveVouchers = voucherRepository.findByTrangThaiNot(2); // Lấy tất cả voucher chưa kết thúc
        for (Voucher voucher : allActiveVouchers) {
            if (voucher.getSoLuong() <= 0) {
                voucherRepository.updateVoucherStatusById(voucher.getId(), 2, now);
            }
        }
    }
}