package com.example.datnsd26.services;

import com.example.datnsd26.models.KhuyenMai;
import com.example.datnsd26.models.KhuyenMaiChitiet;
import com.example.datnsd26.models.SanPhamChiTiet;
import com.example.datnsd26.repository.KhuyenMaiChiTietRepository;
import com.example.datnsd26.repository.KhuyenMaiRepository;
import com.example.datnsd26.repository.SanPhamChiTietRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class KhuyenMaiSchedulerService {

    private final KhuyenMaiRepository khuyenMaiRepository;
    private final KhuyenMaiChiTietRepository khuyenMaiChiTietRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final CapNhatGiaKMServie capNhatGiaKMServie;

    private final ConcurrentHashMap<Long, Timer> timerMap = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initAfterStartup() {
        List<KhuyenMai> danhSach = khuyenMaiRepository.findAll();
        danhSach.forEach(this::scheduleKhuyenMai);
    }


    public void scheduleKhuyenMai(KhuyenMai km) {
        LocalDateTime now = LocalDateTime.now();
        long delayToStart = Duration.between(now, km.getThoiGianBatDau()).toMillis();
        long delayToEnd = Duration.between(now, km.getThoiGianKetThuc()).toMillis();

        Timer timer = new Timer();

        if (now.isBefore(km.getThoiGianBatDau())) {
            km.setTrangThai(0); // Chưa diễn ra
            khuyenMaiRepository.save(km);
            capNhatTrangThaiChiTietKhuyenMai(km.getId(), 0);
        } else if (now.isBefore(km.getThoiGianKetThuc())) {
            km.setTrangThai(1); // Đang hoạt động
            khuyenMaiRepository.save(km);
            capNhatTrangThaiChiTietKhuyenMai(km.getId(), 1);
            capNhatGiaKMServie.capNhatGiaSanPham();
        } else {
            km.setTrangThai(2); // Đã kết thúc
            khuyenMaiRepository.save(km);
            capNhatTrangThaiChiTietKhuyenMai(km.getId(), 2);
            capNhatGiaKMServie.capNhatGiaSanPham();
            return;
        }

        if (delayToStart > 0) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    km.setTrangThai(1);
                    khuyenMaiRepository.save(km);
                    capNhatTrangThaiChiTietKhuyenMai(km.getId(), 1); // 👈
                    capNhatGiaKMServie.capNhatGiaSanPham();
                }
            }, delayToStart);
        }

        if (delayToEnd > 0) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    km.setTrangThai(2);
                    khuyenMaiRepository.save(km);
                    capNhatTrangThaiChiTietKhuyenMai(km.getId(), 2); // 👈
                    capNhatGiaKMServie.capNhatGiaSanPham();
                }
            }, delayToEnd);
        }


        timerMap.put(km.getId(), timer);
    }

    public void rescheduleKhuyenMai(KhuyenMai km) {
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

    private void capNhatTrangThaiChiTietKhuyenMai(Long khuyenMaiId, int newStatus) {
        List<KhuyenMaiChitiet> chiTietList = khuyenMaiChiTietRepository.findByKhuyenMai_Id(khuyenMaiId);
        for (KhuyenMaiChitiet chiTiet : chiTietList) {
            chiTiet.setTrangThai(newStatus);
        }
        khuyenMaiChiTietRepository.saveAll(chiTietList);
    }

    @PreDestroy
    public void shutdownScheduler() {
        timerMap.values().forEach(Timer::cancel);
    }
}
