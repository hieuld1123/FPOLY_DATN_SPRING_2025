package com.example.datnsd26.controller;


import com.example.datnsd26.Dto.NhanVienTKDto;
import com.example.datnsd26.models.NhanVien;
import com.example.datnsd26.models.TaiKhoan;
import com.example.datnsd26.services.FileLoadService;
import com.example.datnsd26.services.NhanVienService;
import com.example.datnsd26.services.TaiKhoanService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RequestMapping("/nhan-vien")
@Controller
public class NhanVienController {
    @Autowired
    private NhanVienService nhanVienService;
    @Autowired
    private TaiKhoanService taiKhoanService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private FileLoadService fileLoadService;


    @GetMapping("/hien-thi")
    public String hienThiNhanVien(Model model,
                                  @RequestParam("page") Optional<Integer> pageParam) {
        int page = (Integer) pageParam.orElse(0);
        Pageable p = PageRequest.of(page, 5, Sort.by(Sort.Direction.DESC, new String[]{"id"}));
        Page<NhanVien> listNV = nhanVienService.findAll(p);
        model.addAttribute("nhanVien", listNV);
        return "/admin/nhan-vien/danh-sach-nhan-vien";
    }

    @GetMapping("/tim-kiem")
    public String timKiemNV(@RequestParam("page") Optional<Integer> pageParam,
                            @RequestParam(value = "searchInput", required = false) String tenSdtMaE,
                            @RequestParam(value = "statusOption", required = false) Boolean trangThai,
                            @RequestParam(value = "roleOption", required = false) String role,
                            Model model) {
        int page = pageParam.orElse(0);
        Pageable p = PageRequest.of(page, 5);

        tenSdtMaE = (tenSdtMaE == null || tenSdtMaE.trim().isEmpty()) ? "" : tenSdtMaE.trim().replaceAll("\\s+", " ");

        TaiKhoan.Role vaiTro = null;
        if (role != null && !role.isEmpty()) {
            try {
                vaiTro = TaiKhoan.Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                vaiTro = null;
            }
        }

        Page<NhanVien> listNV = nhanVienService.findByTenSdtMaTT(tenSdtMaE, trangThai, vaiTro == null ? null : vaiTro.name(), p);

        model.addAttribute("nhanVien", listNV);
        model.addAttribute("searchInput", tenSdtMaE);
        model.addAttribute("statusOption", trangThai);
        model.addAttribute("roleOption", role);

        return "admin/nhan-vien/danh-sach-nhan-vien :: #nhanVienTable";
    }


    @GetMapping("/them")
    public String hienThiThemNhanVien(Model model) {
        model.addAttribute("taiKhoan", taiKhoanService.getAll());
        model.addAttribute("nhanVienDto", new NhanVienTKDto());
        return "/admin/nhan-vien/view-them";
    }

    @PostMapping("/them")
    public String themNhanVien(@ModelAttribute("nhanVienDto") NhanVienTKDto nhanVienTKDto,
                               @RequestParam(name = "anh", required = false) MultipartFile anh) throws MessagingException {
        if (anh != null && !anh.isEmpty()) {
            System.out.println("Ảnh được gửi lên: " + anh.getOriginalFilename());
            // Lưu ảnh vào thư mục
            String fileName = fileLoadService.uploadFile(anh);
            System.out.println("Tên file sau khi lưu: " + fileName);

            nhanVienTKDto.setHinhAnh(fileName != null ? fileName : "default.jpg");
        } else {
            System.out.println("Không có ảnh nào được gửi lên.");
            nhanVienTKDto.setHinhAnh("default.jpg");  // Đặt ảnh mặc định nếu không có ảnh
        }

        nhanVienService.save(nhanVienTKDto);
        return "redirect:/nhan-vien/hien-thi";
    }




    @GetMapping("/chi-tiet/{id}")
    public String chiTietNhanVien(@PathVariable("id") Integer id,
                                  Model model) {
        NhanVien nhanVien = nhanVienService.getById(id);
        NhanVienTKDto nhanVienTKDto = new NhanVienTKDto();
        nhanVienTKDto.setHinhAnh(nhanVien.getHinhAnh());
        nhanVienTKDto.setMaNhanvien(nhanVien.getMaNhanvien());
        nhanVienTKDto.setTenNhanVien(nhanVien.getTenNhanVien());
        nhanVienTKDto.setEmail(nhanVien.getIdTaiKhoan().getEmail());
        nhanVienTKDto.setId(nhanVien.getId());
        nhanVienTKDto.setDiaChiCuThe(nhanVien.getDiaChiCuThe());
        nhanVienTKDto.setGioiTinh(nhanVien.getGioiTinh());
        nhanVienTKDto.setXa(nhanVien.getXa());
        nhanVienTKDto.setHuyen(nhanVien.getHuyen());
        nhanVienTKDto.setTinh(nhanVien.getTinh());
        nhanVienTKDto.setHinhAnh(nhanVien.getHinhAnh());
        nhanVienTKDto.setNgaySinh(nhanVien.getNgaySinh());
        nhanVienTKDto.setNgayCapNhat(nhanVien.getNgayCapNhat());
        nhanVienTKDto.setNgayTao(nhanVien.getNgayTao());
        nhanVienTKDto.setSdt(nhanVien.getIdTaiKhoan().getSdt());
        nhanVienTKDto.setVaiTro(nhanVien.getIdTaiKhoan().getVaiTro());
        nhanVienTKDto.setTrangThai(nhanVien.getTrangThai());
        model.addAttribute("nhanVien", nhanVienTKDto);
        model.addAttribute("listNV", nhanVienService.getAll());
        model.addAttribute("listTK", taiKhoanService.getAll());
        return "/admin/nhan-vien/view-chi-tiet";
    }

    @GetMapping("/hien-thi-sua/{id}")
    public String hienThiSuaNhanVien(@PathVariable("id") Integer id,
                                     Model model) {
        NhanVien nhanVien = nhanVienService.getById(id);
        NhanVienTKDto nhanVienTKDto = new NhanVienTKDto();
        nhanVienTKDto.setHinhAnh(nhanVien.getHinhAnh());
        nhanVienTKDto.setMaNhanvien(nhanVien.getMaNhanvien());
        nhanVienTKDto.setTenNhanVien(nhanVien.getTenNhanVien());
        nhanVienTKDto.setEmail(nhanVien.getIdTaiKhoan().getEmail());
        nhanVienTKDto.setId(nhanVien.getId());
        nhanVienTKDto.setDiaChiCuThe(nhanVien.getDiaChiCuThe());
        nhanVienTKDto.setGioiTinh(nhanVien.getGioiTinh());
        nhanVienTKDto.setXa(nhanVien.getXa());
        nhanVienTKDto.setHuyen(nhanVien.getHuyen());
        nhanVienTKDto.setTinh(nhanVien.getTinh());
        nhanVienTKDto.setHinhAnh(nhanVien.getHinhAnh());
        nhanVienTKDto.setNgaySinh(nhanVien.getNgaySinh());
        nhanVienTKDto.setNgayCapNhat(nhanVien.getNgayCapNhat());
        nhanVienTKDto.setNgayTao(nhanVien.getNgayTao());
        nhanVienTKDto.setSdt(nhanVien.getIdTaiKhoan().getSdt());
        nhanVienTKDto.setVaiTro(nhanVien.getIdTaiKhoan().getVaiTro());
        nhanVienTKDto.setTrangThai(nhanVien.getTrangThai());
        model.addAttribute("nhanVienDto", nhanVienTKDto);
        model.addAttribute("listNV", nhanVienService.getAll());
        model.addAttribute("listTK", taiKhoanService.getAll());
        return "/admin/nhan-vien/view-sua";
    }

    @PostMapping("/sua/{id}")
    private String suaNhanVien(@ModelAttribute("nhanVien") NhanVienTKDto nhanVienTKDto,
                               @PathVariable Integer id,
                               @RequestParam(name = "anh") MultipartFile anh) {
        if (!anh.isEmpty()) {
            String fileName = fileLoadService.uploadFile(anh);
            nhanVienTKDto.setHinhAnh(fileName);
        }
        nhanVienService.update(nhanVienTKDto, id);
        return "redirect:/nhan-vien/hien-thi";
    }

}
