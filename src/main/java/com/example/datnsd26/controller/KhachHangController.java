package com.example.datnsd26.controller;

import com.example.datnsd26.Dto.DiaChiDTO;
import com.example.datnsd26.Dto.KhachHangDto;
import com.example.datnsd26.Dto.NhanVienTKDto;
import com.example.datnsd26.models.DiaChi;
import com.example.datnsd26.models.KhachHang;
import com.example.datnsd26.models.NhanVien;
import com.example.datnsd26.models.TaiKhoan;
import com.example.datnsd26.repository.KhachHangRepository;
import com.example.datnsd26.services.*;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/khach-hang")
public class KhachHangController {
    @Autowired
    private KhachHangService khachHangService;
    @Autowired
    private DiaChiService diaChiService;
    @Autowired
    KhachHangRepository khachHangRepository;
    @Autowired
    private FileLoadService fileLoadService;
    @Autowired
    TaiKhoanService taiKhoanService;

    @GetMapping("/hien-thi")
    public String hienThiKhachHang(@RequestParam("page") Optional<Integer> pageParam,
                                   Model model) {
        int page = (Integer) pageParam.orElse(0);
        Pageable p = PageRequest.of(page, 5, Sort.by(Sort.Direction.DESC, new String[]{"id"}));
        Page<KhachHang> listKH = khachHangService.findAll(p);
        model.addAttribute("listKH", listKH);
        return "/admin/khach-hang/danh-sach";
    }

    @GetMapping("/tim-kiem")
    public String timKiemNV(@RequestParam("page") Optional<Integer> pageParam,
                            @RequestParam(value = "searchInput", required = false) String tenSdtMaE,
                            @RequestParam(value = "statusOption", required = false) Boolean trangThai,
                            Model model) {
        int page = pageParam.orElse(0);
        Pageable p = PageRequest.of(page, 5);

        tenSdtMaE = (tenSdtMaE == null || tenSdtMaE.trim().isEmpty()) ? "" : tenSdtMaE.trim().replaceAll("\\s+", " ");

        Page<KhachHang> listKH = khachHangService.findByTenSdtMaTT(tenSdtMaE, trangThai, p);

        model.addAttribute("listKH", listKH);
        model.addAttribute("searchInput", tenSdtMaE);
        model.addAttribute("statusOption", trangThai);

        return "/admin/khach-hang/danh-sach :: #khachHangTable";
    }

    @GetMapping("/them")
    public String hienThithemKhachHang(Model model) {
        KhachHangDto khachHangDto = new KhachHangDto();

        if (khachHangDto.getListDiaChi() == null) {
            khachHangDto.setListDiaChi(new ArrayList<>());
        }
        model.addAttribute("taiKhoan", taiKhoanService.getAll());
        model.addAttribute("khachHangDto", khachHangDto);
        return "/admin/khach-hang/view-them";
    }

    @PostMapping("/them")
    public String themKhachHang(@ModelAttribute("khachHangDto") KhachHangDto khachHangDto,
                                @RequestParam("diaChiMacDinhId") Integer diaChiMacDinhIndex,
                                @RequestParam(name = "anh") MultipartFile anh) throws MessagingException {

        if (anh != null && !anh.isEmpty()) {
            System.out.println("Ảnh được gửi lên: " + anh.getOriginalFilename());
            // Lưu ảnh vào thư mục
            String fileName = fileLoadService.uploadFile(anh);
            System.out.println("Tên file sau khi lưu: " + fileName);

            khachHangDto.setHinhAnh(fileName != null ? fileName : "default.jpg");
        } else {
            System.out.println("Không có ảnh nào được gửi lên.");
            khachHangDto.setHinhAnh("default.jpg");  // Đặt ảnh mặc định nếu không có ảnh
        }

      for (int i = 0; i < khachHangDto.getListDiaChi().size(); i++) {
            khachHangDto.getListDiaChi().get(i).setTrangThai(false);
        }

        if (diaChiMacDinhIndex != null && diaChiMacDinhIndex >= 0 &&
                diaChiMacDinhIndex < khachHangDto.getListDiaChi().size()) {
            khachHangDto.getListDiaChi().get(diaChiMacDinhIndex).setTrangThai(true);
        }

        khachHangService.save(khachHangDto);
        return "redirect:/khach-hang/hien-thi";
    }

    @GetMapping("/chi-tiet/{id}")
    public String hienThiKhachHang(@PathVariable("id") Integer id,
                                   Model model) {
        KhachHang khachHang = khachHangService.getById(id);
        KhachHangDto khachHangDto = new KhachHangDto();
        khachHangDto.setId(khachHang.getId());
        khachHangDto.setHinhAnh(khachHang.getHinhAnh());
        khachHangDto.setMaKhachHang(khachHang.getMaKhachHang());
        khachHangDto.setTenKhachHang(khachHang.getTenKhachHang());
        khachHangDto.setEmail(khachHang.getTaiKhoan().getEmail());
        khachHangDto.setSdt(khachHang.getTaiKhoan().getSdt());
        khachHangDto.setTrangThai(khachHang.getTrangThai());
        khachHangDto.setVaiTro(khachHang.getTaiKhoan().getVaiTro());
        khachHangDto.setGioiTinh(khachHang.getGioiTinh());
        khachHangDto.setNgaySinh(khachHang.getNgaySinh());
        khachHangDto.setNgayTao(khachHang.getNgayTao());
        khachHangDto.setNgayCapNhat(khachHang.getNgayCapNhat());

        List<DiaChi> danhSachDiaChi = diaChiService.findByKhachHangId(id);

        List<DiaChiDTO> listDiaChiDTO = danhSachDiaChi.stream()
                .filter(DiaChi::getTrangThai)
                .map(diaChi -> {
                    DiaChiDTO dto = new DiaChiDTO();
                    dto.setId(diaChi.getId());
                    dto.setTinh(diaChi.getTinh());
                    dto.setHuyen(diaChi.getHuyen());
                    dto.setXa(diaChi.getXa());
                    dto.setDiaChiCuThe(diaChi.getDiaChiCuThe());
                    dto.setTrangThai(diaChi.getTrangThai());
                    return dto;
                }).collect(Collectors.toList());

        model.addAttribute("khachHangDto", khachHangDto);
        model.addAttribute("listDiaChi", listDiaChiDTO);

        return "/admin/khach-hang/view-chi-tiet";
    }

    @GetMapping("/sua/{id}")
    public String hienThiSuaKhachHang(@PathVariable("id") Integer id,
                                      Model model) {
        KhachHang khachHang = khachHangService.getById(id);
        KhachHangDto khachHangDto = new KhachHangDto();

        khachHangDto.setId(khachHang.getId());
        khachHangDto.setHinhAnh(khachHang.getHinhAnh());
        khachHangDto.setMaKhachHang(khachHang.getMaKhachHang());
        khachHangDto.setTenKhachHang(khachHang.getTenKhachHang());
        khachHangDto.setEmail(khachHang.getTaiKhoan().getEmail());
        khachHangDto.setSdt(khachHang.getTaiKhoan().getSdt());
        khachHangDto.setTrangThai(khachHang.getTrangThai());
        khachHangDto.setVaiTro(khachHang.getTaiKhoan().getVaiTro());
        khachHangDto.setGioiTinh(khachHang.getGioiTinh());
        khachHangDto.setNgaySinh(khachHang.getNgaySinh());
        khachHangDto.setNgayTao(khachHang.getNgayTao());
        khachHangDto.setNgayCapNhat(khachHang.getNgayCapNhat());

        List<DiaChi> danhSachDiaChi = diaChiService.findByKhachHangId(id);
        if (danhSachDiaChi == null) {
            danhSachDiaChi = new ArrayList<>();
        }
        List<DiaChiDTO> listDiaChiDTO = danhSachDiaChi.stream().map(diaChi -> {
            DiaChiDTO dto = new DiaChiDTO();
            dto.setId(diaChi.getId());
            dto.setTinh(diaChi.getTinh());
            dto.setHuyen(diaChi.getHuyen());
            dto.setXa(diaChi.getXa());
            dto.setDiaChiCuThe(diaChi.getDiaChiCuThe());
            dto.setTrangThai(diaChi.getTrangThai());
            return dto;
        }).collect(Collectors.toList());
        khachHangDto.setListDiaChi(listDiaChiDTO);

        Integer macDinhId = danhSachDiaChi.stream()
                .filter(DiaChi::getTrangThai) // Lọc địa chỉ có trạng thái true (mặc định)
                .findFirst()
                .map(DiaChi::getId)
                .orElse(null);
        khachHangDto.setDiaChiMacDinhId(macDinhId);

        model.addAttribute("khachHang", khachHangDto);
        return "/admin/khach-hang/view-sua";
    }

    @PostMapping("/sua/{id}")
    public String suaKhachHang(@ModelAttribute("khachHangDto") KhachHangDto khachHangDto,
                               @RequestParam("diaChiMacDinhId") Integer diaChiMacDinhIndex,
                               @RequestParam(name = "anh") MultipartFile anh,
                               @PathVariable Integer id) {
        if (!anh.isEmpty()) {
            String fileName = fileLoadService.uploadFile(anh);
            khachHangDto.setHinhAnh(fileName);
        }

        // Cập nhật mặc định
//        for (DiaChiDTO diaChiDTO : khachHangDto.getListDiaChi()) {
//            diaChiDTO.setTrangThai(false);
//
//            if (diaChiDTO.getId() != null && diaChiDTO.getId().equals(diaChiMacDinhIndex)) {
//                diaChiDTO.setTrangThai(true);
//            }
//
//        }
        khachHangService.update(khachHangDto, id);
        return "redirect:/khach-hang/hien-thi";
    }

    @PostMapping("/xoa-dia-chi/{id}")
    public String xoaDC(@PathVariable("id") Integer id) {
        diaChiService.xoaDiaChiTheoId(id);
        return "redirect:/khach-hang/sua/{id}";

    }
}
