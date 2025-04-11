package com.example.datnsd26.services;

import com.example.datnsd26.models.Voucher;
import com.example.datnsd26.repository.VoucherRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherSchedulerService {

    private final VoucherRepository voucherRepository;
    private final VoucherStatusUpdater voucherStatusUpdater;
    private final ConcurrentHashMap<Long, Timer> timerMap = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void initAfterStartup() {
        List<Voucher> danhSach = voucherRepository.findAll();
        danhSach.forEach(this::scheduleKhuyenMai);
    }

    public void scheduleKhuyenMai(Voucher km) {
        LocalDateTime now = LocalDateTime.now();
        long delayToStart = Duration.between(now, km.getNgayBatDau()).toMillis();
        long delayToEnd = Duration.between(now, km.getNgayKetThuc()).toMillis();

        Timer timer = new Timer();

        if (now.isBefore(km.getNgayBatDau())) {
            km.setTrangThai(0); // Chưa diễn ra
            voucherRepository.save(km);
        } else if (now.isBefore(km.getNgayKetThuc())) {
            km.setTrangThai(1); // Đang hoạt động
            voucherRepository.save(km);
        } else {
            km.setTrangThai(2); // Đã kết thúc
            voucherRepository.save(km);
            return;
        }

        if (delayToStart > 0) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    voucherStatusUpdater.updateVoucherStatus();
                }
            }, delayToStart);
        }

        if (delayToEnd > 0) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    voucherStatusUpdater.updateVoucherStatus();
                }
            }, delayToEnd);
        }

        timerMap.put(km.getId(), timer);
    }

    public void rescheduleKhuyenMai(Voucher km) {
        Timer oldTimer = timerMap.remove(km.getId());
        if (oldTimer != null) {
            oldTimer.cancel();
        }
        scheduleKhuyenMai(km);
    }

    public void cancelScheduledKhuyenMai(Long kmId) {
        Timer timer = timerMap.remove(kmId);
        if (timer != null) {
            timer.cancel();
        }
    }

    @PreDestroy
    public void shutdownScheduler() {
        timerMap.values().forEach(Timer::cancel);
    }
}
