package com.example.datnsd26.services;


import com.example.datnsd26.repository.HoaDonChiTietRepository;
import com.example.datnsd26.repository.HoaDonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class ThongKeService {

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    public Float layDoanhThuTheoKhoang(Date start, Date end) {
        Float doanhThu = hoaDonChiTietRepository.getDoanhThuTrongKhoang(start, end);
        return doanhThu != null ? doanhThu : 0f;
    }
    public Float layDoanhThuHomNay(Date today) {
        Float doanhThu = hoaDonChiTietRepository.getDoanhThuHomNay(today);
        return doanhThu != null ? doanhThu : 0f;
    }
    public Long laySoDonHoanThanhHomNay(Date ngay) {
        return hoaDonRepository.getSoDonHoanThanh(ngay);
    }
    public Long laySoDonHoanThanhTrongKhoang(Date start, Date end) {
        return hoaDonRepository.countDonHoanThanhTrongKhoang(start, end);
    }

    public Long laySoDonHuyHomNay(Date ngay) {
        return hoaDonRepository.countDonDaHuy(ngay);

    }
    public Long laySoDonHuyTrongKhoang(Date start, Date end) {
        return hoaDonRepository.countDonHuyTrongKhoang(start, end);
    }
    public Float layDoanhThuTheoThang(int thang, int nam) {
        Float doanhThu = hoaDonChiTietRepository.getDoanhThuTheoThang(thang, nam);
        return doanhThu != null ? doanhThu : 0f;
    }
    public Float layDoanhThuTheoNam(int nam) {
        Float doanhThu = hoaDonChiTietRepository.getDoanhThuTheoNam(nam);
        return doanhThu != null ? doanhThu : 0f;
    }
    public Long laySoDonHoanThanhTheoThangNam(int thang, int nam) {
        Long count = hoaDonRepository.countDonHoanThanhTheoThangNam(thang, nam);
        return count != null ? count : 0L;
    }
    public Long laySoDonHoanThanhTheoNam(int nam) {
        Long count = hoaDonRepository.countDonHoanThanhTheoNam(nam);
        return count != null ? count : 0L;
    }
    public Long laySoDonHuyTheoThangVaNam(int thang, int nam) {
        Long count = hoaDonRepository.countDonHuyTheoThangVaNam(thang, nam);
        return count != null ? count : 0L;
    }
    public Long laySoDonHuyTheoNam(int nam) {
        Long count = hoaDonRepository.countDonHuyTheoNam(nam);
        return count != null ? count : 0L;
    }

    public double layDoanhThuTheoNgay(LocalDate ngay) {
        return hoaDonRepository.tinhDoanhThuTheoNgay(ngay.atStartOfDay(), ngay.plusDays(1).atStartOfDay());
    }


    public List<Object[]> tongDoanhThuTheoGioTrongNgay(LocalDate ngay) {
        return hoaDonRepository.tongDoanhThuTheoGioTrongNgay(ngay);
    }



}
