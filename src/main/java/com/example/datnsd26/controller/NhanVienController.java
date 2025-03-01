package com.example.datnsd26.controller;


import com.example.datnsd26.Dto.NhanVienTKDto;
import com.example.datnsd26.models.NhanVien;
import com.example.datnsd26.models.TaiKhoan;
import com.example.datnsd26.services.FileLoadService;
import com.example.datnsd26.services.NhanVienService;
import com.example.datnsd26.services.TaiKhoanService;
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
        return "/admin/nhanvien/quanLyNhanVien";
    }

    @GetMapping("/tim-kiem")
    public String timKiemNV(@RequestParam("page") Optional<Integer> pageParam,
                            @RequestParam(value = "searchInput", required = false) String tenSdtMaE,
                            @RequestParam(value = "statusOption", required = false) Boolean trangThai,
                            @RequestParam(value = "roleOption", required = false) String role,
                            Model model) {
        int page = pageParam.orElse(0);
        Pageable p = PageRequest.of(page, 5);

        // Nếu search là null hoặc rỗng, gán giá trị mặc định
        if (tenSdtMaE == null || tenSdtMaE.trim().isEmpty()) {
            tenSdtMaE = "";  // Chỉ tìm kiếm tất cả
        } else {
            // Loại bỏ khoảng trắng ở đầu và cuối chuỗi, và thay thế khoảng trắng liên tiếp bằng một khoảng trắng duy nhất
            tenSdtMaE = tenSdtMaE.trim().replaceAll("\\s+", " ");
        }

        // Nếu vai trò được truyền vào, chuyển nó thành kiểu Enum
        TaiKhoan.Role vaiTro = null;
        if (role != null && !role.isEmpty()) {
            try {
                vaiTro = TaiKhoan.Role.valueOf(role.toUpperCase()); // Chuyển String thành enum
            } catch (IllegalArgumentException e) {
                // Nếu không chuyển đổi được, có thể dùng giá trị mặc định hoặc để null
                vaiTro = null;
            }
        }
        Page<NhanVien> listNV = nhanVienService.findByTenSdtMaTT(tenSdtMaE,trangThai,String.valueOf(vaiTro),p);
        model.addAttribute("nhanVien", listNV);
        model.addAttribute("searchInput", tenSdtMaE);
        model.addAttribute("statusOption", trangThai);
        model.addAttribute("roleOption", role);
        return "/admin/nhanvien/quanLyNhanVien";
    }

    @GetMapping("/them")
    public String hienThiThemNhanVien(Model model) {
        model.addAttribute("nhanVien", nhanVienService.getAll());
        model.addAttribute("taiKhoan", taiKhoanService.getAll());
        model.addAttribute("nhanVienDto", new NhanVienTKDto());
        return"/admin/nhanvien/themNhanVien";
    }

    @PostMapping("/them")
    public String themNhanVien(@ModelAttribute("nhanVienDto") NhanVienTKDto nhanVienTKDto,
                               @RequestParam(name = "anh") MultipartFile anh) {
        String fileName = fileLoadService.uploadFile(anh);
        if (fileName != null) {
            nhanVienTKDto.setHinhAnh(fileName);
        }
//        String to = a.getEmail();
//        String subject = "Chúc mừng đã trở thành nhân viên của T&T shop";
//        String mailType = "";
//        String mailContent = "Tài khoản của bạn là: " + a.getEmail() +"\nMật khẩu của bạn là: "+ matKhau;
//        taiKhoan.sendEmail(to, subject, mailType, mailContent);
//        checkthem=1;
//        session.setAttribute("themthanhcong",checkthem);
        nhanVienService.save(nhanVienTKDto);
        return "redirect:/nhan-vien/hien-thi";
    }


    @GetMapping("/hien-thi/{id}")
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
        model.addAttribute("nhanVien", nhanVienTKDto);
        model.addAttribute("listNV", nhanVienService.getAll());
        model.addAttribute("listTK", taiKhoanService.getAll());
        return "/admin/nhanvien/suaNhanVien";
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
