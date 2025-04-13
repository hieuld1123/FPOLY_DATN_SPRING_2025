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
            voucherRepository.updateVoucherStatusById(voucher.getId(), 1, now);
        }

        // Cập nhật trạng thái cho voucher hết hạn hoặc hết số lượng
        List<Voucher> activeVouchers = voucherRepository.findByTrangThai(1);
        for (Voucher voucher : activeVouchers) {
            if (voucher.getSoLuong() <= 0 || now.isAfter(voucher.getNgayKetThuc())) {
                voucherRepository.updateVoucherStatusById(voucher.getId(), 2, now);
            }
        }

        // Cập nhật trạng thái cho voucher đến thời gian kết thúc
        List<Voucher> toExpire = voucherRepository.findVouchersToExpire(now);
        for (Voucher voucher : toExpire) {
            voucherRepository.updateVoucherStatusById(voucher.getId(), 2, now);
        }
    }
}
