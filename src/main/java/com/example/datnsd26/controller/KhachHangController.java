package com.example.datnsd26.controller;

import com.example.datnsd26.Dto.DiaChiDTO;
import com.example.datnsd26.Dto.KhachHangDto;
import com.example.datnsd26.Dto.NhanVienTKDto;
import com.example.datnsd26.models.DiaChi;
import com.example.datnsd26.models.KhachHang;
import com.example.datnsd26.repository.KhachHangRepository;
import com.example.datnsd26.services.DiaChiService;
import com.example.datnsd26.services.KhachHangService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
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

    @GetMapping("/hien-thi")
    public String hienThiKhachHang(Model model) {
        model.addAttribute("listKH", khachHangService.getAll());
        return "/admin/khachhang/quanLyKhachHang";
    }

    @GetMapping("/them")
    public String hienThithemKhachHang(Model model) {
        KhachHangDto khachHangDto = new KhachHangDto();
        khachHangDto.setListDiaChi(new ArrayList<>());
        model.addAttribute("khachHangDto", khachHangDto);
        return "/admin/khachhang/themKhachHang";
    }

    @PostMapping("/them")
    public String themKhachHang(@ModelAttribute("khachHangDto") KhachHangDto khachHangDto) {
        khachHangService.save(khachHangDto);
        return "redirect:/khach-hang/hien-thi";
    }
    @GetMapping("/hien-thi/{id}")
    public String hienThiKhachHang(@PathVariable("id") Integer id,
                                     Model model) {
        KhachHang khachHang = khachHangService.getById(id);
        KhachHangDto khachHangDto = new KhachHangDto();
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

        model.addAttribute("khachHangDto", khachHangDto);
        model.addAttribute("listDiaChi", listDiaChiDTO);

        return "/admin/khachhang/hienThiKhachHang";
    }

    @GetMapping("/hien-thi-sua/{id}")
    public String hienThiSuaKhachHang(@PathVariable("id") Integer id,
                                   Model model) {
        KhachHang khachHang = khachHangService.getById(id);
        KhachHangDto khachHangDto = new KhachHangDto();
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

        model.addAttribute("khachHangDto", khachHangDto);
        return "/admin/khachhang/suaKhachHang";
    }
    @PostMapping("/sua/{id}")
    public String suaKhachHang(@ModelAttribute("khachHangDto") KhachHangDto khachHangDto,
                               @PathVariable Integer id) {
        khachHangService.update(khachHangDto,id);
        return "redirect:/khach-hang/hien-thi";
    }
}
