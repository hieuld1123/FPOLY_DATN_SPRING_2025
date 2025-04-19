package com.example.datnsd26.controller;

import com.example.datnsd26.Dto.SanPhamSapHetDto;
import com.example.datnsd26.Dto.SanPhamThongKeDTO;
import com.example.datnsd26.models.HoaDon;
import com.example.datnsd26.repository.HoaDonRepository;
import com.example.datnsd26.services.SanPhamService;
import com.example.datnsd26.services.ThongKeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/thong-ke")
public class ThongKeController {
    @Autowired
    private SanPhamService sanPhamService;
    @Autowired
    private ThongKeService thongKeService;

    @GetMapping
    public String thongKe(@RequestParam(required = false) String kieuLoc,
                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date tuNgay,
                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date denNgay,
                          @RequestParam(required = false) Integer thang,
                          @RequestParam(required = false) Integer nam,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "5") int pageSize,
                          Model model) {
        Date today = new Date();

        // Mặc định: Doanh thu, đơn hoàn thành và đơn hủy trong ngày
        Float doanhThuHomNay = thongKeService.layDoanhThuHomNay(today);
        Long donHoanThanhHomNay = thongKeService.laySoDonHoanThanhHomNay(today);
        Long donHuyHomNay = thongKeService.laySoDonHuyHomNay(today);

        model.addAttribute("doanhThu", doanhThuHomNay);
        model.addAttribute("donHangHomNay", donHoanThanhHomNay);
        model.addAttribute("donHuyHomNay", donHuyHomNay);

        // Biểu đồ doanh thu theo giờ hôm nay
        LocalDate homNay = LocalDate.now();
        List<Object[]> doanhThuTheoGio = thongKeService.tongDoanhThuTheoGioTrongNgay(homNay);
        List<String> gioTrongNgay = new ArrayList<>();
        List<Double> doanhThuTheoGioList = new ArrayList<>();


        for (Object[] obj : doanhThuTheoGio) {
            Integer gio = (Integer) obj[0];
            Double doanhThu = ((Number) obj[1]).doubleValue();
            gioTrongNgay.add(gio + "h");
            doanhThuTheoGioList.add(doanhThu);
        }
        model.addAttribute("gioTrongNgay", gioTrongNgay);
        model.addAttribute("doanhThuTheoGio", doanhThuTheoGioList);

        // Biến để render biểu đồ
        List<String> ngayLoc = new ArrayList<>();
        List<Double> doanhThuLoc = new ArrayList<>();


        // Lọc theo khoảng ngày
        if ("ngay".equals(kieuLoc) && tuNgay != null && denNgay != null) {
            Float doanhThuKhoang = thongKeService.layDoanhThuTheoKhoang(tuNgay, denNgay);
            Long donHoanThanhKhoang = thongKeService.laySoDonHoanThanhTrongKhoang(tuNgay, denNgay);
            Long donHuyKhoang = thongKeService.laySoDonHuyTrongKhoang(tuNgay, denNgay);

            model.addAttribute("doanhThuKhoang", doanhThuKhoang);
            model.addAttribute("soDonHoanThanhKhoang", donHoanThanhKhoang);
            model.addAttribute("soDonHuyKhoang", donHuyKhoang);
            model.addAttribute("tuNgay", tuNgay);
            model.addAttribute("denNgay", denNgay);

            // Tạo dữ liệu biểu đồ
            LocalDate start = tuNgay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate end = denNgay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            List<LocalDate> days = start.datesUntil(end.plusDays(1)).collect(Collectors.toList());

            for (LocalDate day : days) {
                ngayLoc.add(day.format(DateTimeFormatter.ofPattern("dd-MM")));
                double doanhThu = thongKeService.layDoanhThuTheoNgay(day);
                doanhThuLoc.add(doanhThu);
            }
        }

        // Lọc theo tháng
        if ("thang".equals(kieuLoc) && thang != null && nam != null) {
            Float doanhThuThang = thongKeService.layDoanhThuTheoThang(thang, nam);
            Long donHoanThanhThang = thongKeService.laySoDonHoanThanhTheoThangNam(thang, nam);
            Long donHuyThang = thongKeService.laySoDonHuyTheoThangVaNam(thang, nam);

            model.addAttribute("doanhThuTheoThang", doanhThuThang);
            model.addAttribute("soDonHoanThanhThang", donHoanThanhThang);
            model.addAttribute("soDonHuyThang", donHuyThang);
            model.addAttribute("thang", thang);
            model.addAttribute("nam", nam);

            // Biểu đồ theo từng ngày trong tháng
            YearMonth ym = YearMonth.of(nam, thang);
            for (int day = 1; day <= ym.lengthOfMonth(); day++) {
                LocalDate date = ym.atDay(day);
                ngayLoc.add(date.format(DateTimeFormatter.ofPattern("dd-MM")));
                double doanhThu = thongKeService.layDoanhThuTheoNgay(date);
                doanhThuLoc.add(doanhThu);
            }
        }

        // Lọc theo năm
        if ("nam".equals(kieuLoc) && nam != null) {
            Float doanhThuNam = thongKeService.layDoanhThuTheoNam(nam);
            Long donHoanThanhNam = thongKeService.laySoDonHoanThanhTheoNam(nam);
            Long donHuyNam = thongKeService.laySoDonHuyTheoNam(nam);

            model.addAttribute("doanhThuTheoNam", doanhThuNam);
            model.addAttribute("soDonHoanThanhNam", donHoanThanhNam);
            model.addAttribute("soDonHuyNam", donHuyNam);
            model.addAttribute("nam", nam);

            for (int month = 1; month <= 12; month++) {
                ngayLoc.add("Th " + month);
                double doanhThu = thongKeService.layDoanhThuTheoThang(month, nam);
                doanhThuLoc.add(doanhThu);
            }
        }

        // Nếu không có kiểu lọc -> mặc định theo giờ
        if (kieuLoc == null) {
            model.addAttribute("ngayLoc", gioTrongNgay);
            model.addAttribute("doanhThuLoc", doanhThuTheoGioList);
            model.addAttribute("bieuDoMau", "gio");
        } else {
            model.addAttribute("ngayLoc", ngayLoc);
            model.addAttribute("doanhThuLoc", doanhThuLoc);
            model.addAttribute("bieuDoMau", "khac");
        }

        // Dữ liệu biểu đồ
        model.addAttribute("ngayLoc", ngayLoc);
        model.addAttribute("doanhThuLoc", doanhThuLoc);

        // Thống kê sản phẩm
        List<SanPhamThongKeDTO> banChay = sanPhamService.layTopSanPhamBanChay(5);
        List<SanPhamSapHetDto> sapHet = sanPhamService.layTopSanPhamSapHet(page, pageSize);

        model.addAttribute("topSanPhamBanChay", banChay);
        model.addAttribute("topSanPhamSapHet", sapHet);
        model.addAttribute("page", page);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("kieuLoc", kieuLoc);


        return "/admin/thong-ke/thong-ke";
    }

}
